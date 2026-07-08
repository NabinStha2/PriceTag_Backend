package com.example.pricetag.features.product.dto.response;

import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String primaryImageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private String brand;
    private Double totalRating;
    private Integer totalReview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryResponseDto category;
    private SubCategoryResponseDto subCategory;
}
