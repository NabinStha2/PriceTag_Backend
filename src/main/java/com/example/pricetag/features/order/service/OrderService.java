package com.example.pricetag.features.order.service;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.features.order.dto.requestDto.CreateOrderRequestDto;
import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderSummaryResponseDto;

import java.util.List;

public interface OrderService {
    CommonResponseDto<CreateOrderResponseDto> createOrder(
            CreateOrderRequestDto createOrderRequestDto);

    CommonResponseDto<List<OrderSummaryResponseDto>> getAllOrdersByUser(
            PaginationRequestDto paginationRequestDto);
}
