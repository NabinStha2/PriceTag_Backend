package com.example.pricetag.features.cart.controller;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;
import com.example.pricetag.features.cart.dto.requestDto.UpdateCartItemQuantityRequestDto;
import com.example.pricetag.features.cart.dto.responseDto.CartResponseDto;
import com.example.pricetag.features.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @PostMapping("/items")
    public ResponseEntity<CommonResponseDto<Void>> addItemToCart(
            @RequestBody
            @Valid
            AddCartItemRequestDto addCartItemRequestDto) {
        return ResponseEntity.ok(cartService.addItemToCart(addCartItemRequestDto));
    }

    @GetMapping("/")
    public ResponseEntity<CommonResponseDto<CartResponseDto>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }


    @PatchMapping("/item/{itemId}")
    public ResponseEntity<CommonResponseDto<CartResponseDto>> updateCartItemQuantity(
            @PathVariable
            Long itemId,
            @RequestBody
            @Valid
            UpdateCartItemQuantityRequestDto updateCartItemQuantityRequestDto) {
        return ResponseEntity.ok(
                cartService.updateCartItemQuantity(itemId, updateCartItemQuantityRequestDto));
    }
}
