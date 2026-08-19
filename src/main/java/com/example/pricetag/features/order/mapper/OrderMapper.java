package com.example.pricetag.features.order.mapper;

import com.example.pricetag.features.order.dto.responseDto.*;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.entity.OrderItem;
import com.example.pricetag.features.order.entity.ShippingAddressSnapshot;
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

    @Mapping(target = "itemId",
            source = "id")
    @Mapping(target = "variantId",
            source = "variants.id")
    OrderItemResponseDto mapOrderItemToOrderItemResponseDto(OrderItem orderItem);

    ShippingAddressResponseDto mapShippingAddressSnapshotToShippingAddressResponseDto(
            ShippingAddressSnapshot shippingAddressSnapshot);

    @Mapping(target = "orderId",
            source = "id")
    @Mapping(target = "items",
            source = "orderItems")
    @Mapping(target = "shippingAddress",
            source = "shippingAddressSnapshot")
    OrderDetailsResponseDto mapOrderToOrderDetailsResponseDto(Order order);
}
