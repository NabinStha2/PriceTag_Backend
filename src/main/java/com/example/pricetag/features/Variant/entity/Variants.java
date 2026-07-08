package com.example.pricetag.features.Variant.entity;

import com.example.pricetag.features.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "variants",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"size_id", "color_id", "product_id"})},
        indexes = {@Index(columnList = "product_id",
                name = "idx_product_id"), @Index(columnList = "sku",
                name = "idx_sku"), @Index(columnList = "color_id",
                name = "idx_color_id"), @Index(columnList = "size_id",
                name = "idx_size_id"),})
public class Variants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    // SKU Code (VERY IMPORTANT)
    @Column(name = "sku",
            nullable = false,
            unique = true,
            length = 100)
    private String sku;

    @Column(name = "stock_quantity",
            nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "actual_price",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal actualPrice;

    @Column(name = "discounted_price",
            precision = 10,
            scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "weight_in_grams")
    private Integer weightInGrams;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

}
