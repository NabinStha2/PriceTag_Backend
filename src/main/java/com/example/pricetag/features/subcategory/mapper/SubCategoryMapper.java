package com.example.pricetag.features.subcategory.mapper;

import com.example.pricetag.features.subcategory.dto.request.CreateSubCategoryRequestDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.example.pricetag.features.subcategory.entity.SubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubCategoryMapper {
    @Mapping(source = "category.id",
            target = "categoryId")
    SubCategoryResponseDto mapSubCategoryToSubCategoryDto(SubCategory subCategory);

    List<SubCategoryResponseDto> mapSubCategoryListToSubCategoryDtoList(
            List<SubCategory> subCategories);

    SubCategory mapCreateSubCategoryRequestDtoToSubCategory(
            CreateSubCategoryRequestDto createSubCategoryRequestDto);
}
