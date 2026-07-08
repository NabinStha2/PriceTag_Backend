package com.example.pricetag.dto;

import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.product.dto.request.CreateProductRequestDto;
import com.example.pricetag.features.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {

    private Long id;

    private User user;

    private CreateProductRequestDto product;

    private Integer quantity;

    private BigDecimal checkoutAmt;

    private Variants variants;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
