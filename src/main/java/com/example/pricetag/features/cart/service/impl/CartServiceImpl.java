package com.example.pricetag.features.cart.service.impl;

import com.example.pricetag.features.cart.dto.requestDto.AddCartItemRequestDto;
import com.example.pricetag.features.cart.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Override
    public void addItemToCart(AddCartItemRequestDto addCartItemRequestDto) {
        
    }
}
