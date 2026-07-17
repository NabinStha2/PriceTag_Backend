package com.example.pricetag.features.cart.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * One Cart row per user, enforced by the unique constraint below - status
 * tracks this row's lifecycle, it does NOT mean multiple cart rows exist
 * per user. See CartServiceImpl for exactly how status transitions:
 * ACTIVE    - normal shopping state, fully editable
 * CHECKOUT  - locked while payment is in progress (future Order module
 * sets this); addItem/updateItemQuantity/removeItem/
 * clearCart all reject with 409 while in this state
 * ABANDONED - stale cart flagged by a future cleanup job; auto-flips
 * back to ACTIVE the moment the user edits it again
 */
@Entity
@Table(name = "carts",
        indexes = {@Index(name = "idx_cart_user",
                columnList = "user_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"},
                name = "uk_cart_user")})
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    // nullable = false + @Builder.Default together mean a Cart can never
    // exist with a null status, even if a future caller builds one without
    // explicitly setting .status(...).
    @Column(name = "status",
            nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    public enum CartStatus {
        ACTIVE, CHECKOUT, ABANDONED
    }
}