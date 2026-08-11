package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
@Builder
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
            nullable = false)
    private String orderNumber;

    @Column(name = "order_status",
            nullable = false,
            length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "total_amt")
    private BigDecimal totalAmt;

    @Column(name = "shipping_amt")
    private BigDecimal shippingAmt;

    @Column(name = "tax_amt")
    private BigDecimal taxAmt;

    @Column(name = "discount_amt")
    private BigDecimal discountAmt;

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
