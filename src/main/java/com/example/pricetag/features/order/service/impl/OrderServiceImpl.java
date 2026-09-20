package com.example.pricetag.features.order.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginatedResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.address.entity.Address;
import com.example.pricetag.features.address.repository.AddressRepo;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.cart.service.CartAccessService;
import com.example.pricetag.features.order.dto.requestDto.CancelOrderRequestDto;
import com.example.pricetag.features.order.dto.requestDto.CreateOrderRequestDto;
import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderDetailsResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderSummaryResponseDto;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.entity.OrderItem;
import com.example.pricetag.features.order.entity.ShippingAddressSnapshot;
import com.example.pricetag.features.order.mapper.OrderMapper;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.order.service.OrderLifecycleService;
import com.example.pricetag.features.order.service.OrderService;
import com.example.pricetag.features.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Set<String> ALLOWED_ORDER_SORT_FIELDS = Set.of("createdAt", "orderNumber",
                                                                        "status", "paymentStatus",
                                                                        "totalAmt");
    private final AuthService authService;
    private final CartRepo cartRepo;
    private final AddressRepo addressRepo;
    private final VariantRepo variantRepo;
    private final OrderRepo orderRepo;
    private final OrderMapper orderMapper;
    private final OrderLifecycleService orderLifecycleService;
    private final CartAccessService cartAccessService;

    public OrderServiceImpl(AuthService authService, CartRepo cartRepo, AddressRepo addressRepo,
                            VariantRepo variantRepo, OrderRepo orderRepo, OrderMapper orderMapper,
                            OrderLifecycleService orderLifecycleService,
                            CartAccessService cartAccessService) {
        this.authService = authService;
        this.cartRepo = cartRepo;
        this.addressRepo = addressRepo;
        this.variantRepo = variantRepo;
        this.orderRepo = orderRepo;
        this.orderMapper = orderMapper;
        this.orderLifecycleService = orderLifecycleService;
        this.cartAccessService = cartAccessService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommonResponseDto<CreateOrderResponseDto> createOrder(
            CreateOrderRequestDto createOrderRequestDto, String idempotencyKey) {
        String checkoutKey = normalizeCheckoutKey(idempotencyKey);
        String requestHash = fingerprint(createOrderRequestDto);

        User authenticatedUser = authService.getUser();

        // Serialize this user's cart and order-creation operations.
        User user = cartAccessService.lockUser(authenticatedUser);

        Order existingOrder = orderRepo
                .findByUserIdAndCheckoutKey(user.getId(), checkoutKey)
                .orElse(null);

        if (existingOrder != null) {
            if (!requestHash.equals(existingOrder.getCheckoutRequestHash())) {
                throw new ApplicationException("IDEMPOTENCY_KEY_REUSED",
                                               "This checkout key was already used with different details",
                                               HttpStatus.CONFLICT);
            }

            return CommonResponseDto
                    .<CreateOrderResponseDto>builder()
                    .success(true)
                    .status(HttpStatus.CREATED.value())
                    .message("Order created. Payment is required.")
                    .data(orderMapper.mapOrderToCreateOrderResponseDto(existingOrder))
                    .build();
        }

         /*
           * Lock the cart so another checkout request cannot
           process it
           * concurrently.
           */
        Cart cart = cartAccessService.requireExisting(user);
        validateCart(cart);

        /*
           * Querying using both addressId and userId verifies
           ownership.
           */
        Address address = addressRepo
                .findByIdAndUserId(createOrderRequestDto.getAddressId(), user.getId())
                .orElseThrow(
                        () -> new ApplicationException("404", "Shipping Address not found for user",
                                                       HttpStatus.NOT_FOUND));

          /*
           * Sort IDs so concurrent checkouts acquire variant
           locks in
           * a consistent order. This reduces deadlock risk.
           */
        List<Long> variantIds = cart
                .getCartItems()
                .stream()
                .map(item -> item
                        .getVariant()
                        .getId())
                .distinct()
                .sorted()
                .toList();

        List<Variants> lockedVariants = variantRepo.findAllByIdForUpdate(variantIds);
        if (lockedVariants.size() != variantIds.size()) {
            throw new ApplicationException("409", "One or more cart products no longer exist",
                                           HttpStatus.CONFLICT);
        }
        Map<Long, Variants> variantsById = lockedVariants
                .stream()
                .collect(Collectors.toMap(Variants::getId, v -> v));

        ShippingAddressSnapshot shippingAddressSnapshot = createShippingAddressSnapshot(address,
                                                                                        createOrderRequestDto);

        Order order = Order
                .builder()
                .user(user)
                .shippingAddressSnapshot(shippingAddressSnapshot)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING_PAYMENT)
                .checkoutKey(checkoutKey)
                .checkoutRequestHash(requestHash)
                .build();

        final BigDecimal[] subtotal = {BigDecimal.ZERO};
        cart

                .getCartItems()
                .forEach(cartItem -> {
                    Long variantId = cartItem
                            .getVariant()
                            .getId();
                    Variants variant = variantsById.get(variantId);
                    validateVariantAndStock(variant, cartItem.getQuantity());

                     /*
               * Recalculate price using the current database
               price.
               * Never trust a total sent by the frontend.
               */
                    BigDecimal unitPrice = resolveUnitPrice(variant);
                    BigDecimal totalPrice = unitPrice.multiply(
                            BigDecimal.valueOf(cartItem.getQuantity()));

                    log.info("Total Price: {} for SKU: {} and quantity: {} and unit price: {}",
                             totalPrice, variant.getSku(), cartItem.getQuantity(), unitPrice);

                    OrderItem orderItem = OrderItem
                            .builder()
                            .variants(variant)
                            .colorName(variant.getColor() == null ? null : variant
                                                                           .getColor()
                                                                           .getName())
                            .sizeName(variant.getSize() == null ? null : variant
                                                                         .getSize()
                                                                         .getName())
                            .quantity(cartItem.getQuantity())
                            .unitPrice(unitPrice)
                            .totalPrice(totalPrice)
                            .sku(variant.getSku())
                            .productName(variant
                                                 .getProduct()
                                                 .getName())
                            .imageUrl(variant
                                              .getProduct()
                                              .getPrimaryImageUrl())
                            .build();

                    /*
               * This helper establishes both sides of the
               relationship.
               */
                    order.addOrderItem(orderItem);
                    subtotal[0] = subtotal[0].add(totalPrice);


                    /*
               * Reserve stock while payment is pending.
               * Payment failure or order expiry must add it back
               later.
               */
                    variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
                });

        order.setShippingAmt(subtotal[0].compareTo(BigDecimal.valueOf(100)) >= 0 ? BigDecimal.ZERO :
                             BigDecimal.valueOf(10));
        order.setTaxAmt(subtotal[0].multiply(BigDecimal.valueOf(0.1))); // 10% tax
        order.setDiscountAmt(BigDecimal.ZERO); // No discount logic implemented yet
        order.setSubtotalAmt(subtotal[0]);
        order.setTotalAmt(subtotal[0]
                                  .subtract(order.getDiscountAmt())
                                  .add(order.getShippingAmt())
                                  .add(order.getTaxAmt()));
        order.setExpiresAt(LocalDateTime
                                   .now()
                                   .plusMinutes(60)); // Order expires in 60 minutes

         /*
           * OrderItem entities are saved automatically because
           Order has
           * cascade = CascadeType.ALL.
           */
        Order savedOrder = orderRepo.save(order);

        // Checkout consumes the current cart contents immediately.
        cart
                .getCartItems()
                .clear();
        cart.setStatus(CartStatus.ACTIVE);


        return CommonResponseDto
                .<CreateOrderResponseDto>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Order created. Payment is required.")
                .data(orderMapper.mapOrderToCreateOrderResponseDto(savedOrder))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<OrderSummaryResponseDto>> getAllOrdersByUser(
            PaginationRequestDto paginationRequestDto) {

        User user = authService.getUser();

        int requestedPage = paginationRequestDto != null ? paginationRequestDto.getPage() : 1;
        int requestedLimit = paginationRequestDto != null ? paginationRequestDto.getLimit() : 10;
        String sortBy = paginationRequestDto != null && paginationRequestDto.getSortBy() != null ?
                        ALLOWED_ORDER_SORT_FIELDS.contains(paginationRequestDto.getSortBy()) ?
                        paginationRequestDto.getSortBy() : "createdAt" : "createdAt";
        String sortDir = paginationRequestDto != null && paginationRequestDto.getOrder() != null ?
                         paginationRequestDto.getOrder() : Sort.Direction.DESC.name();


        /*
         * API pages start at 1, but Spring Data pages start at 0.
         */
        int page = Math.max(requestedPage, 1) - 1;
        /*
         * Prevent a request such as limit=100000 from loading too
         * many orders into memory.
         */
        int limit = Math.clamp(requestedLimit, 1, 100);
        Sort.Direction dir =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.Direction.ASC :
                Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, limit, Sort.by(dir, sortBy));
        Page<Order> orderPage = orderRepo.findAllByUserId(user.getId(), pageable);

        List<OrderSummaryResponseDto> orderSummaryResponseDtosList = orderMapper.mapOrderListToOrderSummaryResponseDtoList(
                orderPage.getContent());

        return CommonResponseDto
                .<List<OrderSummaryResponseDto>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Orders retrieved successfully")
                .data(orderSummaryResponseDtosList)
                .pagination(PaginatedResponseDto.from(orderPage))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<OrderDetailsResponseDto> getSingleOrderDetailsByUser(
            String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new ApplicationException("400", "Order number is required",
                                           HttpStatus.BAD_REQUEST);
        }

        User user = authService.getUser();

        Order order = orderRepo
                .findByOrderNumberAndUserId(orderNumber, user.getId())
                .orElseThrow(() -> new ApplicationException("404", "Order not found for user",
                                                            HttpStatus.NOT_FOUND));

        return CommonResponseDto
                .<OrderDetailsResponseDto>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Order retrieved successfully")
                .data(orderMapper.mapOrderToOrderDetailsResponseDto(order))
                .build();
    }

    @Override
    public CommonResponseDto<OrderDetailsResponseDto> cancelOrder(String orderNumber,
                                                                  CancelOrderRequestDto cancelOrderRequestDto) {
        User user = authService.getUser();
        Order order = orderLifecycleService.cancelPendingOrder(orderNumber, user.getId(),
                                                               cancelOrderRequestDto.getReason());

        return CommonResponseDto
                .<OrderDetailsResponseDto>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Order cancelled successfully")
                .data(orderMapper.mapOrderToOrderDetailsResponseDto(order))
                .build();
    }


    // Helper methods ---------------------------------------------------------------------
    private void validateCart(Cart cart) {
        if (cart.getStatus() == CartStatus.CHECKOUT) {
            throw new ApplicationException("409", "Cart is locked while checkout is in progress",
                                           HttpStatus.CONFLICT);
        }
        if (cart.getCartItems() == null || cart
                .getCartItems()
                .isEmpty()) {
            throw new ApplicationException("400", "Cart is empty", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateVariantAndStock(Variants variant, Integer requestedQuantity) {
        if (variant == null || !Boolean.TRUE.equals(variant.getIsActive()) ||
            Boolean.TRUE.equals(variant.getIsDeleted())) {
            throw new ApplicationException("404", "Product variant not found or inactive",
                                           HttpStatus.NOT_FOUND);
        }

        if (!Boolean.TRUE.equals(variant
                                         .getProduct()
                                         .getIsActive()) || Boolean.TRUE.equals(variant
                                                                                        .getProduct()
                                                                                        .getIsDeleted())) {
            throw new ApplicationException("409", "A cart product is no longer available",
                                           HttpStatus.CONFLICT);
        }

        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new ApplicationException("400", "Invalid quantity", HttpStatus.BAD_REQUEST);
        }

        if (requestedQuantity > variant.getStockQuantity()) {
            throw new ApplicationException("409", "Insufficient stock. Only " +
                                                  variant.getStockQuantity() +
                                                  " left in stock for SKU: " + variant.getSku(),
                                           HttpStatus.CONFLICT);
        }

    }


    private BigDecimal resolveUnitPrice(Variants variant) {
        BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() :
                               variant.getActualPrice();

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApplicationException("409", "Invalid price for SKU " + variant.getSku(),
                                           HttpStatus.CONFLICT);
        }

        return unitPrice;
    }


    private ShippingAddressSnapshot createShippingAddressSnapshot(Address address,
                                                                  CreateOrderRequestDto createOrderRequestDto) {
        return ShippingAddressSnapshot
                .builder()
                .sourceAddressId(address.getId())
                .recipientName(createOrderRequestDto.getRecipientName())
                .phone(createOrderRequestDto.getPhoneNumber())
                .unitNumber(address.getUnitNumber())
                .streetNumber(address.getStreetNumber())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    private String generateOrderNumber() {
        String date = LocalDate
                .now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String randomPart = UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();

        return "PT-" + date + "-" + randomPart;
    }

    private String normalizeCheckoutKey(String rawKey) {
        if (rawKey == null || !rawKey.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" +
                                              "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            throw new ApplicationException("INVALID_IDEMPOTENCY_KEY",
                                           "Idempotency-Key must be a valid UUID",
                                           HttpStatus.BAD_REQUEST);
        }

        return UUID
                .fromString(rawKey)
                .toString();
    }

    private String fingerprint(CreateOrderRequestDto request) {
        if (request == null || request.getAddressId() == null ||
            request.getRecipientName() == null || request.getPhoneNumber() == null) {

            throw new ApplicationException("INVALID_CHECKOUT_REQUEST",
                                           "Address, recipient name, and phone number are required",
                                           HttpStatus.BAD_REQUEST);
        }

        String recipientName = request.getRecipientName();
        String phoneNumber = request.getPhoneNumber();

        // Length prefixes prevent ambiguous field boundaries.
        String payload =
                "checkout-v1|" + request.getAddressId() + "|" + recipientName.length() + ":" +
                recipientName + "|" + phoneNumber.length() + ":" + phoneNumber;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));

            return HexFormat
                    .of()
                    .formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }


    // ------------------------------------------------------------------------------------
}
