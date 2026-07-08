package com.example.pricetag.features.product.dto.response;

import com.example.pricetag.features.Variant.dto.responseDto.VariantResponseDto;
import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.media.dto.response.MediaResponseDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleProductDetailsResponseDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String primaryImageUrl; // Main image URL for quick access
    private List<MediaResponseDto> imageUrl;                   // main/primary image
    private BigDecimal basePrice;              // MRP/base price
    private BigDecimal discountedPrice;        // final / offer price if available
    private String brand;
    private Double totalRating;
    private Integer totalReview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategoryResponseDto category;
    private SubCategoryResponseDto subCategory;

    private List<VariantResponseDto> variants;
}
