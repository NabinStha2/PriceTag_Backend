package com.example.pricetag.features.order.mapper;

import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderSummaryResponseDto;
import com.example.pricetag.features.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "orderId",
            source = "id")
    @Mapping(target = "totalAmount",
            source = "totalAmt")
    CreateOrderResponseDto mapOrderToCreateOrderResponseDto(Order order);

    @Mapping(target = "orderId",
            source = "id")
    @Mapping(target = "totalAmount",
            source = "totalAmt")
    OrderSummaryResponseDto mapOrderToOrderSummaryResponseDto(Order order);

    List<OrderSummaryResponseDto> mapOrderListToOrderSummaryResponseDtoList(List<Order> orders);
}
