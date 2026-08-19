package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_order_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_order_status",
                        columnList = "order_status"
                ),
                @Index(
                        name = "idx_order_payment_status",
                        columnList = "payment_status"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_number",
                        columnNames = "order_number"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "order_number",
            nullable = false,
            length = 50
    )
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;


    // =========================
    // MONEY
    // =========================

    @Column(
            name = "subtotal_amt",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotalAmt = BigDecimal.ZERO;

    @Column(
            name = "discount_amt",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discountAmt = BigDecimal.ZERO;

    @Column(
            name = "shipping_amt",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal shippingAmt = BigDecimal.ZERO;

    @Column(
            name = "tax_amt",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal taxAmt = BigDecimal.ZERO;

    @Column(
            name = "total_amt",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal totalAmt = BigDecimal.ZERO;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "AUD";


    // =========================
    // SHIPPING SNAPSHOT
    // =========================

    @Embedded
    private ShippingAddressSnapshot shippingAddressSnapshot;


    // =========================
    // PAYMENT
    // =========================

    @Column(
            name = "payment_reference",
            length = 200
    )
    private String paymentReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;


    // =========================
    // SHIPPING
    // =========================

    @Column(
            name = "tracking_number",
            length = 100
    )
    private String trackingNumber;


    // =========================
    // CANCELLATION
    // =========================

    @Column(
            name = "cancellation_reason",
            length = 500
    )
    private String cancellationReason;


    // =========================
    // INVENTORY SAFETY
    // =========================

    @Column(
            name = "stock_restored",
            nullable = false
    )
    @Builder.Default
    private boolean stockRestored = false;


    // =========================
    // CONCURRENCY
    // =========================

    @Version
    private Long version;


    // =========================
    // RELATIONSHIPS
    // =========================

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();


    // =========================
    // HELPERS
    // =========================

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void addStatusHistory(OrderStatusHistory history) {
        statusHistory.add(history);
        history.setOrder(this);
    }
}