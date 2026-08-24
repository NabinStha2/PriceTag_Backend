package com.example.pricetag.features.Variant.service.impl;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.enums.EntityType;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import com.example.pricetag.features.Variant.dto.responseDto.VariantResponseDto;
import com.example.pricetag.features.Variant.entity.Color;
import com.example.pricetag.features.Variant.entity.Size;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.mapper.VariantMapper;
import com.example.pricetag.features.Variant.repository.ColorRepo;
import com.example.pricetag.features.Variant.repository.SizeRepo;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.Variant.service.VariantService;
import com.example.pricetag.features.media.dto.response.MediaResponseDto;
import com.example.pricetag.features.media.service.MediaService;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VariantServiceImpl implements VariantService {
    private final VariantRepo variantRepo;
    private final ProductRepo productRepo;
    private final VariantMapper variantMapper;
    private final SizeRepo sizeRepo;
    private final ColorRepo colorRepo;
    private final MediaService mediaService;

    @Override
    @Transactional
    public CommonResponseDto<List<VariantResponseDto>> createVariant(
            List<VariantRequestDto> variantRequestDto, Long productId) {
        Product existingProduct = productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));

        List<Variants> variantList = new ArrayList<>();
        for (VariantRequestDto vDto : variantRequestDto) {
            if (vDto.getSku() != null && variantRepo.existsBySku(vDto.getSku())) {
                throw new ApplicationException("400", "Variant with the same SKU already exists",
                                               HttpStatus.BAD_REQUEST);
            }

            Size size = null;
            Color color = null;

            if (vDto.getSizeId() != null) {
                size = sizeRepo
                        .findSizeById((vDto.getSizeId()))
                        .orElseThrow(() -> new ApplicationException("404", "Size not found",
                                                                    HttpStatus.NOT_FOUND));
            }

            if (vDto.getColorId() != null) {
                color = colorRepo
                        .findColorById(vDto.getColorId())
                        .orElseThrow(() -> new ApplicationException("404", "Color not found",
                                                                    HttpStatus.NOT_FOUND));
            }

            Variants variantData = variantMapper.mapVariantRequestDtoToVariant(vDto,
                                                                               existingProduct,
                                                                               size, color);
            variantList.add(variantData);
        }

        List<Variants> newVariants = variantRepo.saveAll(variantList);

        List<VariantResponseDto> variantResponseDtos = getVariantMedias(newVariants);


        return CommonResponseDto
                .<List<VariantResponseDto>>builder()
                .data(variantResponseDtos)
                .status(401)
                .success(true)
                .message("Variant created successfully")
                .build();
    }

    @Override
    public CommonResponseDto<List<VariantResponseDto>> getAllVariants() {
        List<Variants> variants = variantRepo.findAllByIsActiveTrue();

        List<VariantResponseDto> variantResponseDtos = getVariantMedias(variants);

        return CommonResponseDto
                .<List<VariantResponseDto>>builder()
                .data(variantResponseDtos)
                .status(200)
                .success(true)
                .message("Variants fetched successfully")
                .build();
    }

    @Override
    public CommonResponseDto<VariantResponseDto> getVariantById(Long variantId) {
        Variants variant = variantRepo
                .findByIdAndIsActiveTrue(variantId)
                .orElseThrow(() -> new ApplicationException("404", "Variant not found",
                                                            HttpStatus.NOT_FOUND));

        List<VariantResponseDto> variantResponseDtos = getVariantMedias(
                Collections.singletonList(variant));

        return CommonResponseDto
                .<VariantResponseDto>builder()
                .data(variantResponseDtos.getFirst())
                .status(200)
                .success(true)
                .message("Variant details fetched successfully")
                .build();
    }

    @Override
    public CommonResponseDto<List<VariantResponseDto>> getVariantsByProductId(Long productId) {
        productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));

        List<Variants> variantsList = variantRepo.findAllByProductIdAndIsActiveTrue(productId);

        List<VariantResponseDto> variantResponseDtos = getVariantMedias(variantsList);

        return CommonResponseDto
                .<List<VariantResponseDto>>builder()
                .data(variantResponseDtos)
                .status(200)
                .success(true)
                .message("Variants fetched successfully")
                .build();
    }

    @Override
    @Transactional
    public CommonResponseDto<VariantResponseDto> updateVariant(
            VariantRequestDto variantRequestDto) {
        Variants existingVariant = variantRepo
                .findByIdAndIsActiveTrue(variantRequestDto.getId())
                .orElseThrow(() -> new ApplicationException("404", "Variant not found",
                                                            HttpStatus.NOT_FOUND));


        if (variantRequestDto.getSku() != null && !variantRequestDto
                .getSku()
                .equals(existingVariant.getSku()) &&
            variantRepo.existsBySku(variantRequestDto.getSku())) {

            throw new ApplicationException("409", "Variant SKU already exists",
                                           HttpStatus.CONFLICT);
        }

        if (variantRequestDto.getSizeId() != null) {
            Size size = sizeRepo
                    .findById(variantRequestDto.getSizeId())
                    .orElseThrow(() -> new ApplicationException("404", "Size not found",
                                                                HttpStatus.NOT_FOUND));
            existingVariant.setSize(size);
        }
        if (variantRequestDto.getColorId() != null) {
            Color color = colorRepo
                    .findById(variantRequestDto.getColorId())
                    .orElseThrow(() -> new ApplicationException("404", "Color not found",
                                                                HttpStatus.NOT_FOUND));
            existingVariant.setColor(color);
        }
        if (variantRequestDto.getSku() != null) existingVariant.setSku(variantRequestDto.getSku());
        if (variantRequestDto.getStockQuantity() != null)
            existingVariant.setStockQuantity(variantRequestDto.getStockQuantity());
        if (variantRequestDto.getActualPrice() != null)
            existingVariant.setActualPrice(variantRequestDto.getActualPrice());
        if (variantRequestDto.getDiscountedPrice() != null)
            existingVariant.setDiscountedPrice(variantRequestDto.getDiscountedPrice());
        if (variantRequestDto.getWeightInGrams() != null)
            existingVariant.setWeightInGrams(variantRequestDto.getWeightInGrams());

        Variants saved = variantRepo.save(existingVariant);

        List<VariantResponseDto> variantResponseDtoList = getVariantMedias(
                Collections.singletonList(saved));

        return CommonResponseDto
                .<VariantResponseDto>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .data(variantResponseDtoList.getFirst())
                .message("Variant updated")
                .build();
    }

    @Override
    @Transactional
    public CommonResponseDto<VariantResponseDto> deleteVariant(Long variantId) {
        Variants deletedVariant = variantRepo
                .findByIdAndIsActiveTrue(variantId)
                .orElseThrow(() -> new ApplicationException("404", "Variant not found",
                                                            HttpStatus.NOT_FOUND));

        variantRepo.deleteById(variantId);

        return CommonResponseDto
                .<VariantResponseDto>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .data(variantMapper.mapVariantToVariantResponseDto(deletedVariant))
                .message("Variant deleted successfully")
                .build();
    }


    public List<VariantResponseDto> getVariantMedias(List<Variants> variants) {
        return variants
                .stream()
                .map(v -> {
                    VariantResponseDto variantResponseDto = variantMapper.mapVariantToVariantResponseDto(
                            v);
                    List<MediaResponseDto> mediaDtos = mediaService.getmedias(v.getId(),
                                                                              EntityType.VARIANT);
                    if (mediaDtos != null && !mediaDtos.isEmpty()) {
                        variantResponseDto.setImages(mediaDtos);
                        variantResponseDto.setPrimaryImageUrl(mediaDtos
                                                                      .getFirst()
                                                                      .getUrl());
                    }
                    return variantResponseDto;
                })
                .toList();
    }
}
