package com.example.pricetag.features.order.service;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.order.dto.requestDto.CreateOrderRequestDto;
import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    CommonResponseDto<CreateOrderResponseDto> createOrder(
            CreateOrderRequestDto createOrderRequestDto);
}
