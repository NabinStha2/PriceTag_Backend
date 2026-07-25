package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders",
        indexes = {@Index(columnList = "user_id",
                name = "idx_order_user")})
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

    @Column(name = "order_number",
            nullable = false)
    private String orderNumber;


    @Column(name = "total_amt")
    private Double totalAmt;
}
