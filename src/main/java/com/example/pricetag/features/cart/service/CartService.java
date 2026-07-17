package com.example.pricetag.features.cart.service;


import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;

public interface CartService {
    void addItemToCart(AddCartItemRequestDto addCartItemRequestDto);
}

