package com.example.pricetag.features.Variant.dto.responseDto;

import com.example.pricetag.features.media.dto.response.MediaResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariantResponseDto {
    private Long id;
    private String sku;
    private Integer stockQuantity;
    private BigDecimal actualPrice;
    private BigDecimal discountedPrice;
    private Integer weightInGrams;
    private ColorResponseDto color;
    private SizeResponseDto size;
    private String primaryImageUrl;
    private List<MediaResponseDto> images;
}
