package com.example.pricetag.features.cart.service;

import com.example.pricetag.dto.AddCartItemDto;
import com.example.pricetag.dto.CommonResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface CartItemService {

    CommonResponseDto getCart();

    CommonResponseDto createCart(AddCartItemDto addCartItemDto);

    CommonResponseDto deleteCartItem(Long cartItemId);

}
