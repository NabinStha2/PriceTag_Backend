package com.example.pricetag.features.Variant.controller;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import com.example.pricetag.features.Variant.dto.responseDto.VariantResponseDto;
import com.example.pricetag.features.Variant.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variant")
@RequiredArgsConstructor
public class VariantController {
    private final VariantService variantService;


    @PostMapping("/product/{productId}")
    @Transactional
    public ResponseEntity<CommonResponseDto<List<VariantResponseDto>>> createVariant(
            @PathVariable
            Long productId,
            @RequestBody
            @Valid
            List<VariantRequestDto> variantRequestDto) {
        return ResponseEntity.ok(variantService.createVariant(variantRequestDto, productId));
    }


    @GetMapping("/")
    public ResponseEntity<CommonResponseDto<List<VariantResponseDto>>> getAllVariants() {
        return ResponseEntity.ok(variantService.getAllVariants());
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<CommonResponseDto<VariantResponseDto>> getVariantById(
            @PathVariable
            Long variantId) {
        return ResponseEntity.ok(variantService.getVariantById(variantId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<CommonResponseDto<List<VariantResponseDto>>> getVariantByProductId(
            @PathVariable
            Long productId) {
        return ResponseEntity.ok(variantService.getVariantsByProductId(productId));
    }

    @PatchMapping("/edit/{variantId}")
    public ResponseEntity<CommonResponseDto<VariantResponseDto>> updateVariant(
            @PathVariable
            Long variantId,
            @RequestBody
            @Valid
            VariantRequestDto variantRequestDto) {
        variantRequestDto.setId(variantId);
        return ResponseEntity.ok(variantService.updateVariant(variantRequestDto));
    }

    @DeleteMapping("/")
    public ResponseEntity<CommonResponseDto<VariantResponseDto>> deleteVariantById(
            @RequestParam
            Long variantId) {
        return ResponseEntity.ok(variantService.deleteVariant(variantId));
    }

}
