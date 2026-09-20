package com.example.pricetag.features.order.dto.responseDto;


import com.example.pricetag.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponseDto {
    private Long orderId;
    private String orderNumber;

    private OrderStatus status;
    private String paymentStatus;

    private BigDecimal totalAmount;
    private String currency;

    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
