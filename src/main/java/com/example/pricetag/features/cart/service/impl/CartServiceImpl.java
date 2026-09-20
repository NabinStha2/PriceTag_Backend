package com.example.pricetag.features.cart.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;
import com.example.pricetag.features.cart.dto.requestDto.UpdateCartItemQuantityRequestDto;
import com.example.pricetag.features.cart.dto.responseDto.CartItemResponseDto;
import com.example.pricetag.features.cart.dto.responseDto.CartResponseDto;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.entity.CartItem;
import com.example.pricetag.features.cart.mapper.CartMapper;
import com.example.pricetag.features.cart.repository.CartItemRepo;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.cart.service.CartAccessService;
import com.example.pricetag.features.cart.service.CartService;
import com.example.pricetag.features.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final AuthService authService;
    private final VariantRepo variantRepo;
    private final CartItemRepo cartItemRepo;
    private final CartMapper cartMapper;
    private final CartAccessService cartAccessService;

    public CartServiceImpl(AuthService authService, CartRepo cartRepo, VariantRepo variantRepo,
                           CartItemRepo cartItemRepo, CartMapper cartMapper,
                           CartAccessService cartAccessService) {
        this.authService = authService;
        this.variantRepo = variantRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartMapper = cartMapper;
        this.cartAccessService = cartAccessService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommonResponseDto<Void> addItemToCart(AddCartItemRequestDto addCartItemRequestDto) {
        User user = authService.getUser();

        Cart existingCart = getOrCreateCartSafely(user);

        checkCartStatus(existingCart);

        Integer requestedQuantity = addCartItemRequestDto.getQuantity();

        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new ApplicationException("INVALID_QUANTITY", "Quantity must be greater than zero",
                                           HttpStatus.BAD_REQUEST);
        }

        Variants existingVariant = variantRepo
                .findByIdAndIsActiveTrue(addCartItemRequestDto.getVariantId())
                .orElseThrow(() -> new ApplicationException("404", "Variant not found",
                                                            HttpStatus.NOT_FOUND));


        BigDecimal newUnitPrice = existingVariant.getDiscountedPrice() != null ?
                                  existingVariant.getDiscountedPrice() :
                                  existingVariant.getActualPrice();

        if (newUnitPrice == null || newUnitPrice.signum() < 0) {
            throw new ApplicationException("INVALID_PRICE",
                                           "This product does not have a valid price",
                                           HttpStatus.CONFLICT);
        }

        CartItem existingCartItem = existingCart
                .getCartItems()
                .stream()
                .filter(item -> item
                        .getVariant()
                        .getId()
                        .equals(addCartItemRequestDto.getVariantId()))
                .findFirst()
                .orElse(null);

        int currentQuantity = existingCartItem == null ? 0 : existingCartItem.getQuantity();

        long combinedQuantity = (long) currentQuantity + requestedQuantity;

        if (combinedQuantity > existingVariant.getStockQuantity()) {
            throw new ApplicationException("INSUFFICIENT_STOCK",
                                           "Only " + existingVariant.getStockQuantity() +
                                           " left in stock", HttpStatus.BAD_REQUEST);
        }


        if (existingCartItem != null) {
            existingCartItem.setQuantity((int) combinedQuantity);
            existingCartItem.setUnitPrice(newUnitPrice);
        } else {
            CartItem newCartItem = CartItem
                    .builder()
                    .cart(existingCart)
                    .variant(existingVariant)
                    .quantity(requestedQuantity)
                    .unitPrice(newUnitPrice)
                    .build();
            existingCart
                    .getCartItems()
                    .add(newCartItem);
            cartItemRepo.save(newCartItem);
        }

        return CommonResponseDto
                .<Void>builder()
                .message("Cart Item created successfully")
                .success(true)
                .status(HttpStatus.CREATED.value())
                .build();
    }

    @Override
    public CommonResponseDto<CartResponseDto> getCart() {
        User user = authService.getUser();
        Cart existingCart = getOrCreateCartSafely(user);

        CartResponseDto cartResponseDto = toCartResponseData(existingCart);

        return CommonResponseDto
                .<CartResponseDto>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Cart fetched successfully")
                .data(cartResponseDto)
                .build();
    }

    @Override
    public CommonResponseDto<CartResponseDto> updateCartItemQuantity(Long itemId,
                                                                     UpdateCartItemQuantityRequestDto updateCartItemQuantityRequestDto) {

        User user = authService.getUser();
        Cart existingCart = getOrCreateCartSafely(user);

        checkCartStatus(existingCart);

        CartItem existingCartItem = cartItemRepo
                .findByIdAndCartId(itemId, existingCart.getId())
                .orElseThrow(() -> new ApplicationException("404", "Cart Item not found",
                                                            HttpStatus.NOT_FOUND));

        if (!existingCartItem
                .getVariant()
                .getIsActive()) {
            throw new ApplicationException("400", "This product is no longer available",
                                           HttpStatus.BAD_REQUEST);
        }
        if (updateCartItemQuantityRequestDto.getQuantity() > existingCartItem
                .getVariant()
                .getStockQuantity()) {
            throw new ApplicationException("400", "Only " + existingCartItem
                    .getVariant()
                    .getStockQuantity() + " left in stock", HttpStatus.BAD_REQUEST);
        }

        BigDecimal newUnitPrice = existingCartItem
                                          .getVariant()
                                          .getDiscountedPrice() != null ? existingCartItem
                                                                          .getVariant()
                                                                          .getDiscountedPrice() :
                                  existingCartItem
                                  .getVariant()
                                  .getActualPrice();

        existingCartItem.setQuantity(updateCartItemQuantityRequestDto.getQuantity());
        existingCartItem.setUnitPrice(newUnitPrice);
        existingCartItem.setTotalPrice(newUnitPrice.multiply(
                BigDecimal.valueOf(updateCartItemQuantityRequestDto.getQuantity())));

        CartResponseDto cartResponseDto = toCartResponseData(existingCart);

        return CommonResponseDto
                .<CartResponseDto>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Cart Item updated successfully")
                .data(cartResponseDto)
                .build();

    }

    @Override
    public CommonResponseDto<CartResponseDto> removeCartItem(Long itemId) {
        User user = authService.getUser();
        Cart existingCart = getOrCreateCartSafely(user);

        checkCartStatus(existingCart);

        CartItem existingCartItem = cartItemRepo
                .findByIdAndCartId(itemId, existingCart.getId())
                .orElseThrow(() -> new ApplicationException("404", "Cart Item not found",
                                                            HttpStatus.NOT_FOUND));

        existingCart
                .getCartItems()
                .remove(existingCartItem);

        CartResponseDto cartResponseDto = toCartResponseData(existingCart);

        return CommonResponseDto
                .<CartResponseDto>builder()
                .success(true)
                .message("Cart Item removed successfully")
                .status(HttpStatus.OK.value())
                .data(cartResponseDto)
                .build();
    }

    public CommonResponseDto<CartResponseDto> clearCart() {
        User user = authService.getUser();
        Cart cart = getOrCreateCartSafely(user);
        checkCartStatus(cart);
        cart
                .getCartItems()
                .clear();

        CartResponseDto cartResponseDto = toCartResponseData(cart);
        return CommonResponseDto
                .<CartResponseDto>builder()
                .success(true)
                .message("All Cart Items are removed successfully")
                .status(HttpStatus.OK.value())
                .data(cartResponseDto)
                .build();
    }


    // Helper Method
    public Cart getOrCreateCartSafely(User user) {
        return cartAccessService.getOrCreate(user);
    }

    public void checkCartStatus(Cart cart) {
        if (cart.getStatus() == CartStatus.CHECKOUT) {
            throw new ApplicationException("409", "Cart is locked while checkout is in progress",
                                           HttpStatus.CONFLICT);
        } else if (cart.getStatus() == CartStatus.ABANDONED) {
            cart.setStatus(CartStatus.ACTIVE);
        }
    }

    public CartResponseDto toCartResponseData(Cart cart) {
        CartResponseDto cartResponseDto = cartMapper.cartToCartResponseDto(cart);

        cartResponseDto.setCartTotal(cartResponseDto
                                             .getItems()
                                             .stream()
                                             .map(CartItemResponseDto::getLineTotal)
                                             .reduce(BigDecimal.ZERO, BigDecimal::add));
        cartResponseDto.setTotalItemCount(cartResponseDto
                                                  .getItems()
                                                  .stream()
                                                  .mapToInt(CartItemResponseDto::getQuantity)
                                                  .sum());

        return cartResponseDto;
    }
}


