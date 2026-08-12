package com.example.pricetag.features.order.dto.responseDto;


import com.example.pricetag.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CreateOrderResponseDto {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
}
