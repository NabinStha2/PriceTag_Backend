package com.example.pricetag.features.cart.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CartStatus status;

    public enum CartStatus {
        ACTIVE, CHECKOUT, ABANDONED
    }
}
