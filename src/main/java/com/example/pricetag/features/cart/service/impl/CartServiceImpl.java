package com.example.pricetag.features.cart.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.entity.CartItem;
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

    public CartServiceImpl(AuthService authService, CartRepo cartRepo, VariantRepo variantRepo,
                           CartItemRepo cartItemRepo) {
        this.authService = authService;
        this.cartRepo = cartRepo;
        this.variantRepo = variantRepo;
        this.cartItemRepo = cartItemRepo;
    }

    @Override
    public CommonResponseDto<Void> addItemToCart(AddCartItemRequestDto addCartItemRequestDto) {
        User user = authService.getUser();
        Cart existingCart = cartRepo
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

        if (existingCart.getStatus() == CartStatus.ABANDONED) {
            existingCart.setStatus(CartStatus.ACTIVE);
        } else if (existingCart.getStatus() == CartStatus.CHECKOUT) {
            throw new ApplicationException("409", "Cart is locked while checkout is in progress",
                                           HttpStatus.CONFLICT);
        }

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
}
