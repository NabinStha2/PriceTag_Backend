package com.example.pricetag.features.ratingReview.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;


@Entity
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "rating_review",
        indexes = {@Index(columnList = "product_id",
                name = "idx_product_id"), @Index(columnList = "user_id",
                name = "idx_user_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "user_id"})})

public class RatingReview extends BaseEntity {

    @Column(name = "rating")
    @Min(value = 0,
            message = "Rating must be at least 0")
    @Max(value = 5,
            message = "Rating must be at most 5")
    private Double rating;

    @Column(name = "review",
            length = 2000)
    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;


}
