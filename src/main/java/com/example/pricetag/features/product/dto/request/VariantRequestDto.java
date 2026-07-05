package com.example.pricetag.features.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequestDto {
    private Long id;
    @NotNull(message = "Size is required")
    private Long sizeId;
    @NotNull(message = "Color is required")
    private Long colorId;
    @NotBlank(message = "SKU is required")
    private String sku;
    @Builder.Default
    private Integer stockQuantity = 0;
    private BigDecimal actualPrice;
    private BigDecimal discountedPrice;
    private Integer weightInGrams;
}

