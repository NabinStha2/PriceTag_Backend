package com.example.pricetag.features.cart.service;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;

public interface CartService {
    CommonResponseDto<Void> addItemToCart(AddCartItemRequestDto addCartItemRequestDto);
}

