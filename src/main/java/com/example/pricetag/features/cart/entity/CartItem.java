package com.example.pricetag.features.cart.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.Variant.entity.Variants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items",
        indexes = {@Index(name = "idx_cart_item_cart",
                columnList = "cart_id"), @Index(name = "idx_cart_item_variant",
                columnList = "variant_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id", "variant_id"},
                name = "uk_cart_item_cart_variant")})
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id",
            nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id",
            nullable = false)
    private Variants variant;

    @Column(name = "quantity",
            nullable = false)
    private Integer quantity;

    @Column(name = "unit_price",
            nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price",
            nullable = false)
    private BigDecimal totalPrice;

    // Helper method to calculate total price
    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

    }
}
