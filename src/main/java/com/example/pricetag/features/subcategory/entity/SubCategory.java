package com.example.pricetag.features.subcategory.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.category.entity.Category;
import com.example.pricetag.features.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
//@ToString
//@RequiredArgsConstructor
@Builder
@Table(name = "sub_categories",
        indexes = {@Index(columnList = "category_id",
                name = "idx_category_id"), @Index(columnList = "category_id, name",
                name = "idx_category_name")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"category_id", "name", "is_deleted"})})
@AllArgsConstructor
@NoArgsConstructor
public class SubCategory extends BaseEntity {

    @Column(name = "name",
            nullable = false)
    private String subCategoryName;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
            nullable = false)
    private Category category;

    @OneToMany(mappedBy = "subCategory",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

}
