package com.example.pricetag.features.order.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.order.dto.requestDto.CreateOrderRequestDto;
import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import com.example.pricetag.features.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Override
    public CommonResponseDto<CreateOrderResponseDto> createOrder(
            CreateOrderRequestDto createOrderRequestDto) {
        
        return null;
    }
}
