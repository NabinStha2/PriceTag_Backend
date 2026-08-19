package com.example.pricetag.features.order.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_status_history",
        indexes = {
                @Index(name = "idx_order_status_history_order",
                        columnList = "order_id"),
                @Index(name = "idx_order_status_history_status",
                        columnList = "new_status")
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",
            nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status",
            length = 30)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status",
            nullable = false,
            length = 30)
    private OrderStatus newStatus;

    // Actor values are snapshots so the audit record does not change if the user changes later.
    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_email",
            length = 255)
    private String actorEmail;

    @Column(name = "note",
            length = 500)
    private String note;
}