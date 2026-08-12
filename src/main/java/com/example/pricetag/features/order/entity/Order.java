package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders",
        indexes = {@Index(columnList = "user_id",
                name = "idx_order_user"), @Index(columnList = "order_number",
                name = "idx_order_order_number")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"order_number"},
                name = "uk_order_order_number")})
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "order_number",
            nullable = false,
            length = 50)
    private String orderNumber;

    @Column(name = "order_status",
            nullable = false,
            length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "total_amt",
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal totalAmt = BigDecimal.ZERO;

    @Column(name = "shipping_amt",
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal shippingAmt = BigDecimal.ZERO;

    @Column(name = "tax_amt",
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal taxAmt = BigDecimal.ZERO;

    @Column(name = "discount_amt",
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal discountAmt = BigDecimal.ZERO;

    @Column(name = "subtotal_amt",
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal subtotalAmt = BigDecimal.ZERO;

    @Column(name = "currency",
            nullable = false,
            length = 3)
    @Builder.Default
    private String currency = "AUD";

    @Embedded
    private ShippingAddressSnapshot shippingAddressSnapshot;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }
}
