package com.example.pricetag.features.product.dto.request;

import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDto {
    private Long productId; // for edit products
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "SubCategory is required")
    private Long subCategoryId;
    @NotBlank(message = "Product name is required")
    private String name;
    private String slug;
    @NotBlank(message = "Description is required")
    private String description;
    private String shortDescription;
    private String brand;
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0",
            inclusive = false,
            message = "Base price must be greater than 0")
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private MultipartFile[] images;
    private List<VariantRequestDto> variants;
}
