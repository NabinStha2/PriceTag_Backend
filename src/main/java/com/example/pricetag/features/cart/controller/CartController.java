package com.example.pricetag.features.cart.controller;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;
import com.example.pricetag.features.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            AddCartItemRequestDto addCartItemRequestDto
                                                                ) {
        return ResponseEntity.ok(cartService.addItemToCart(addCartItemRequestDto));
    }
}
