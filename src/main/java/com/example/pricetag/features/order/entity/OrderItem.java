package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.Variant.entity.Variants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        indexes = {@Index(name = "idx_order_item_order",
                columnList = "order_id"), @Index(name = "idx_order_item_variant",
                columnList = "variant_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"order_id", "variant_id"},
                name = "uk_order_item_order_variant")})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",
            nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id",
            nullable = false)
    private Variants variants;

    @Column(name = "color_name")
    private String colorName;

    @Column(name = "size_name")
    private String sizeName;

    @Column(name = "quantity",
            nullable = false)
    private Integer quantity;

    @Column(name = "unit_price",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "sku",
            nullable = false,
            length = 100)
    private String sku;

    @Column(name = "product_name",
            nullable = false)
    private String productName;

    @Column(name = "image_url")
    private String imageUrl;
}
