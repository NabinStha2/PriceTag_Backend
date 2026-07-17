package com.example.pricetag.features.cart.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Flat, display-ready shape - avoids nesting Variants/Product/Color/Size
 * entities (serialization recursion, lazy-loading pitfalls).
 * <p>
 * unitPrice / lineTotal are mapped directly from CartItem's persisted
 * unitPrice / totalPrice columns (see CartMapper) - they are snapshots
 * taken at add-to-cart time, not live catalog prices. See CartItem entity
 * Javadoc for the exact refresh policy.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {

    private Long itemId;
    private Long variantId;
    private String sku;

    private String productName;
    private String imageUrl;
    private String colorName;
    private String colorHex;
    private String sizeName;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private Integer stockAvailable;    // lets the UI show "only 2 left" / disable + button at max
}