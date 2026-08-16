package com.example.pricetag.features.payment.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.enums.PaymentMethod;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.features.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Table(name = "payments",
        indexes = {@Index(name = "idx_payment_order",
                columnList = "order_id"), @Index(name = "idx_payment_payment_id",
                columnList = "payment_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"payment_id"},
                name = "uk_payment_payment_id")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "order_id",
            nullable = false)
    private Order order;

    @Column(name = "payment_provider",
            nullable = false,
            length = 30)
    private String paymentProvider;

    @Column(name = "payment_id",
            nullable = false,
            length = 200)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",
            nullable = false,
            length = 30)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method",
            nullable = false,
            length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_currency",
            nullable = false,
            length = 3)
    private String paymentCurrency;

    @Column(name = "payment_amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal paymentAmount;


}
