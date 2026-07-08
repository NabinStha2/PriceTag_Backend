package com.example.pricetag.features.subcategory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryResponseDto {
    private Long id;
    private String subCategoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long categoryId;
}