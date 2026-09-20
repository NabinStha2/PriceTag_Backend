package com.example.pricetag.features.order.dto.responseDto;


import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsResponseDto {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal subtotalAmt;
    private BigDecimal discountAmt;
    private BigDecimal shippingAmt;
    private BigDecimal taxAmt;
    private BigDecimal totalAmt;
    private String currency;

    private ShippingAddressResponseDto shippingAddress;
    private List<OrderItemResponseDto> items;

    private String trackingNumber;
    private String cancellationReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
