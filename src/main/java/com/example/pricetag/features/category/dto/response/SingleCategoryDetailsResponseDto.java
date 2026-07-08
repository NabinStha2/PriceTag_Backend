package com.example.pricetag.features.category.dto.response;

import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SingleCategoryDetailsResponseDto {
    private Long id;
    private String name;
    @Builder.Default
    private List<SubCategoryResponseDto> subCategoryResponseDtoList = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
