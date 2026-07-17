package com.example.pricetag.features.cart.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.Variant.entity.Variants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * unitPrice is a SNAPSHOT, not a live read of Variants.actualPrice /
 * discountedPrice. It's set explicitly by CartServiceImpl at write time
 * (see resolveUnitPrice()) - this entity has no way to know the variant's
 * current price on its own, it only knows what it was told.
 * <p>
 * Refresh policy (enforced in CartServiceImpl, not here):
 * - addItem()            -> unitPrice IS refreshed to current price
 * - updateItemQuantity() -> unitPrice is NOT touched, stays locked
 * <p>
 * totalPrice is derived automatically below and should never be set
 * directly from the service layer - always go through quantity/unitPrice
 * and let @PrePersist/@PreUpdate keep it correct.
 */
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

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}