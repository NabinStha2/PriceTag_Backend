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
import com.example.pricetag.features.cart.service.CartService;
import com.example.pricetag.features.user.entity.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final AuthService authService;
    private final CartRepo cartRepo;
    private final VariantRepo variantRepo;
    private final CartItemRepo cartItemRepo;
    private final CartMapper cartMapper;

    public CartServiceImpl(AuthService authService, CartRepo cartRepo, VariantRepo variantRepo,
                           CartItemRepo cartItemRepo, CartMapper cartMapper) {
        this.authService = authService;
        this.cartRepo = cartRepo;
        this.variantRepo = variantRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartMapper = cartMapper;
    }

    @Override
    public CommonResponseDto<Void> addItemToCart(AddCartItemRequestDto addCartItemRequestDto) {
        User user = authService.getUser();

        Cart existingCart = getOrCreateCartSafely(user);

        checkCartStatus(existingCart);

        Variants existingVariant = variantRepo
                .findByIdAndIsActiveTrue(addCartItemRequestDto.getVariantId())
                .orElseThrow(() -> new ApplicationException("404", "Variant not found",
                                                            HttpStatus.NOT_FOUND));


        BigDecimal newUnitPrice = existingVariant.getDiscountedPrice() != null ?
                                  existingVariant.getDiscountedPrice() :
                                  existingVariant.getActualPrice();

        CartItem existingCartItem = cartItemRepo
                .findByCartIdAndVariantId(existingCart.getId(),
                                          addCartItemRequestDto.getVariantId())
                .orElse(null);

        if (existingCartItem != null) {
            int newQuantity = addCartItemRequestDto.getQuantity() + existingCartItem.getQuantity();
            if (newQuantity > existingVariant.getStockQuantity()) {
                throw new ApplicationException("400", "Only " + existingVariant.getStockQuantity() +
                                                      " left in stock", HttpStatus.BAD_REQUEST);
            }
            existingCartItem.setQuantity(newQuantity);
            existingCartItem.setUnitPrice(newUnitPrice);
        } else {
            if (addCartItemRequestDto.getQuantity() > existingVariant.getStockQuantity()) {
                throw new ApplicationException("400", "Only " + existingVariant.getStockQuantity() +
                                                      " left in stock", HttpStatus.BAD_REQUEST);
            }

            try {
                cartItemRepo.saveAndFlush(CartItem
                                                  .builder()
                                                  .cart(existingCart)
                                                  .variant(existingVariant)
                                                  .quantity(addCartItemRequestDto.getQuantity())
                                                  .unitPrice(newUnitPrice)
                                                  .build());
            } catch (DataIntegrityViolationException e) {
                // Lost a race - another request inserted this exact
                // (cart, variant) pair between our findByCartIdAndVariantId
                // check above and this insert (e.g. a double-tapped "add
                // to cart" button firing two requests). Merge into the row
                // that won instead of failing the request.
                CartItem winner = cartItemRepo
                        .findByCartIdAndVariantId(existingCart.getId(), existingVariant.getId())
                        .orElseThrow(() -> new ApplicationException("409",
                                                                    "Cart Item creation conflict. Please try again.",
                                                                    HttpStatus.CONFLICT));
                winner.setQuantity(winner.getQuantity() + addCartItemRequestDto.getQuantity());
                winner.setUnitPrice(newUnitPrice);
            }
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
        return cartRepo
                .findByUserId(user.getId())
                .orElseGet(() -> {
                    try {
                        return cartRepo.saveAndFlush(Cart
                                                             .builder()
                                                             .user(user)
                                                             .build());
                    } catch (DataIntegrityViolationException e) {
                        return cartRepo
                                .findByUserId(user.getId())
                                .orElseThrow(() -> new ApplicationException("409",
                                                                            "Cart creation conflict. Please try again.",
                                                                            HttpStatus.CONFLICT));
                    }
                });
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


