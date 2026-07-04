package com.example.pricetag.features.product.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.entity.Variants;
import com.example.pricetag.features.category.entity.Category;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.example.pricetag.features.subcategory.entity.SubCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@DynamicUpdate
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products",
        indexes = {@Index(columnList = "subcategory_id",
                name = "idx_subcategory_id"), @Index(columnList = "category_id",
                name = "idx_category_id"), @Index(columnList = "brand",
                name = "idx_brand")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"slug"})})
public class Product extends BaseEntity {

    @Column(name = "name",
            nullable = false)
    private String name;

    // SEO-friendly URL slug
    @Column(nullable = false)
    private String slug;  // Example: nike-air-max-red

    // Long description (HTML friendly)
    @Column(name = "description",
            nullable = false,
            columnDefinition = "longtext")
    private String description;

    // Short description for listing
    @Column(name = "short_description",
            length = 500)
    private String shortDescription;

    @Column(name = "primary_image_url",
            length = 1000)
    private String primaryImageUrl; // Main image URL for quick access

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "brand")
    private String brand;

    @Builder.Default
    @Column(name = "total_rating")
    private double totalRating = 0;

    @Builder.Default
    @Column(name = "total_review")
    private Integer totalReview = 0;

    @Column(name = "base_price",
            nullable = false)
    private BigDecimal basePrice;

    @Column(name = "discounted_price")
    private BigDecimal discountedPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
            nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id",
            nullable = false)
    private SubCategory subCategory;

    @OneToMany(mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Variants> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<RatingReview> ratingReviews = new ArrayList<>();

}
