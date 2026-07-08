package com.example.pricetag.features.subcategory.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.subcategory.dto.request.CreateSubCategoryRequestDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface SubCategoryService {
    CommonResponseDto getAllSubCategories();

    CommonResponseDto editSubCategory(SubCategoryResponseDto subCategoryResponseDto);

    CommonResponseDto<Void> createSubCategory(Long categoryId,
                                              CreateSubCategoryRequestDto createSubCategoryRequestDto);

    CommonResponseDto getSubCategoriesWithCategoryId(CategoryResponseDto categoryResponseDto);

    CommonResponseDto deleteSubCategory(Long subCategoryId);

}
