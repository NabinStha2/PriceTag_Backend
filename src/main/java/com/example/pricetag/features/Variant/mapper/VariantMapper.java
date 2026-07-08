package com.example.pricetag.features.Variant.mapper;


import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import com.example.pricetag.features.Variant.dto.responseDto.VariantResponseDto;
import com.example.pricetag.features.Variant.entity.Color;
import com.example.pricetag.features.Variant.entity.Size;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.product.entity.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class VariantMapper {
    @Mapping(target = "stockQuantity",
            ignore = true)
    @Mapping(target = "actualPrice",
            ignore = true)
    public abstract Variants mapVariantRequestDtoToVariant(VariantRequestDto variantRequestDto,
                                                           @Context
                                                           Product product,
                                                           @Context
                                                           Size size,
                                                           @Context
                                                           Color color);

    @AfterMapping
    protected void afterMapping(VariantRequestDto dto,
                                @MappingTarget
                                Variants entity,
                                @Context
                                Product product,
                                @Context
                                Size size,
                                @Context
                                Color color) {
        entity.setProduct(product);
        // defaults / conversions
        entity.setStockQuantity(dto.getStockQuantity() == null ? 0 : dto.getStockQuantity());
        entity.setActualPrice(
                dto.getActualPrice() == null ? product.getBasePrice() : dto.getActualPrice());

        entity.setSize(size);
        entity.setColor(color);
    }


    public abstract VariantResponseDto mapVariantToVariantResponseDto(Variants variant);

    public abstract List<VariantResponseDto> mapVariantListToVariantResponseDtoList(
            List<Variants> variants);
}
