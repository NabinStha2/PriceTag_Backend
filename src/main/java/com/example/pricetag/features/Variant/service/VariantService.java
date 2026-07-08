package com.example.pricetag.features.Variant.service;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import com.example.pricetag.features.Variant.dto.responseDto.VariantResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VariantService {
    CommonResponseDto<List<VariantResponseDto>> createVariant(
            List<VariantRequestDto> variantRequestDto,
            Long productId);

    CommonResponseDto<List<VariantResponseDto>> getAllVariants();

    CommonResponseDto<VariantResponseDto> getVariantById(Long variantId);

    CommonResponseDto<List<VariantResponseDto>> getVariantsByProductId(Long productId);

    CommonResponseDto<VariantResponseDto> updateVariant(
            VariantRequestDto variantRequestDto);

    CommonResponseDto<VariantResponseDto> deleteVariant(Long variantId);
}
