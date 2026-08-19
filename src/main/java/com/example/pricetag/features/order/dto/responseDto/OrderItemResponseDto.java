package com.example.pricetag.features.order.dto.responseDto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long itemId;
    private Long variantId;

    private String sku;
    private String productName;
    private String imageUrl;
    private String colorName;
    private String sizeName;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
