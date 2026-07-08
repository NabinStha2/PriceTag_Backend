package com.example.pricetag.features.category.mapper;

import com.example.pricetag.features.category.dto.request.CreateCategoryRequestDto;
import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.category.dto.response.SingleCategoryDetailsResponseDto;
import com.example.pricetag.features.category.entity.Category;
import com.example.pricetag.features.subcategory.mapper.SubCategoryMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SubCategoryMapper.class})
public interface CategoryMapper {

    // Map Category entity to basic CategoryResponseDto
    @Mapping(source = "categoryName",
            target = "name")
    CategoryResponseDto mapCategoryToCategoryResponseDto(Category category);

    List<CategoryResponseDto> mapCategoryListToCategoryResponseDtoList(List<Category> categories);

    // Map Category entity to SingleCategoryDetailsResponseDto including subcategories
    @Mapping(source = "categoryName",
            target = "name")
    @Mapping(source = "subCategories",
            target = "subCategoryResponseDtoList")
    SingleCategoryDetailsResponseDto mapCategoryToSingleCategoryDetailsResponseDto(
            Category category);

    // Map CreateCategoryRequestDto to Category entity
    @Mapping(source = "name",
            target = "categoryName")
    Category mapCreateCategoryRequestDtoToCategory(
            CreateCategoryRequestDto createCategoryRequestDto);
}