package com.example.pricetag.features.subcategory.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.enums.EntityType;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.category.entity.Category;
import com.example.pricetag.features.category.repository.CategoryRepo;
import com.example.pricetag.features.media.dto.response.MediaResponseDto;
import com.example.pricetag.features.media.service.MediaService;
import com.example.pricetag.features.subcategory.dto.request.CreateSubCategoryRequestDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.example.pricetag.features.subcategory.entity.SubCategory;
import com.example.pricetag.features.subcategory.mapper.SubCategoryMapper;
import com.example.pricetag.features.subcategory.repository.SubCategoryRepo;
import com.example.pricetag.features.subcategory.service.SubCategoryService;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    @Autowired
    private SubCategoryRepo subCategoryRepo;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private SubCategoryMapper subCategoryMapper;

    @Override
    public CommonResponseDto getAllSubCategories() {
        List<SubCategory> subCategories = subCategoryRepo.findAll();

        List<SubCategoryResponseDto> subCategoryResponseDtoList = new ArrayList<>();
        subCategories.forEach((sub) -> {

//            CategoryDto newCategoryDto = new CategoryDto();
//            newCategoryDto.setId(sub.getCategory().getId());
//            newCategoryDto.setCategoryName(sub.getCategory().getCategoryName());

            SubCategoryResponseDto newSubCategoryResponseDto = new SubCategoryResponseDto();
            newSubCategoryResponseDto.setId(sub.getId());
            newSubCategoryResponseDto.setSubCategoryName(sub.getSubCategoryName());
            newSubCategoryResponseDto.setCreatedAt(sub.getCreatedAt());
            newSubCategoryResponseDto.setUpdatedAt(sub.getUpdatedAt());
            newSubCategoryResponseDto.setCategoryId(sub
                                                            .getCategory()
                                                            .getId());

            subCategoryResponseDtoList.add(newSubCategoryResponseDto);
        });

        return CommonResponseDto
                .builder()
                .message("Success")
                .data(Map.of("results", subCategoryResponseDtoList))
                .success(true)
                .build();

    }

    @Override
    public CommonResponseDto editSubCategory(SubCategoryResponseDto subCategoryResponseDto)
            throws ApplicationException {
        Optional<SubCategory> existingSubCategoryOptional = subCategoryRepo.findById(
                subCategoryResponseDto.getId());
        if (existingSubCategoryOptional.isPresent()) {
            SubCategory existingSubCategory = existingSubCategoryOptional.get();

            existingSubCategory.setSubCategoryName(subCategoryResponseDto.getSubCategoryName());
            if (subCategoryResponseDto.getCategoryId() != null) {
                existingSubCategory.setCategory(categoryRepo
                                                        .findById(
                                                                subCategoryResponseDto.getCategoryId())
                                                        .orElseThrow(() -> new ApplicationException(
                                                                "404",
                                                                "Category not found",
                                                                HttpStatus.NOT_FOUND)));
            }

            try {
                SubCategory savedSubCategory = subCategoryRepo.save(existingSubCategory);

                return CommonResponseDto
                        .builder()
                        .message("SubCategory edited successfully")
                        .success(true)
                        .data(Map.of("results", savedSubCategory))
                        .build();

            } catch (DataAccessException ex) {
                throw new ApplicationException("500", "Database error",
                                               HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new ApplicationException("404", " Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public CommonResponseDto<Void> createSubCategory(Long categoryId,
                                                     CreateSubCategoryRequestDto createSubCategoryRequestDto)
            throws ApplicationException {

        Category existingCategory = categoryRepo
                .findById(categoryId)
                .orElseThrow(() -> new ApplicationException("404", "Category not found",
                                                            HttpStatus.NOT_FOUND));

        boolean exists = subCategoryRepo.existsByCategoryIdAndSubCategoryNameIgnoreCaseAndIsDeletedFalse(
                existingCategory.getId(), createSubCategoryRequestDto.getSubCategoryName());

//        Optional<SubCategory> subCategoryFilteredData = existingCategory
//                .getSubCategories()
//                .stream()
//                .filter(data -> data
//                        .getSubCategoryName()
//                        .equals(subCategoryDto.getSubCategoryName()))
//                .findFirst();

        if (exists) {
            throw new ApplicationException("409",
                                           "SubCategory already exists with this name inside this " +
                                           existingCategory.getCategoryName(), HttpStatus.CONFLICT);
        }

        SubCategory newSubCategory = subCategoryMapper.mapCreateSubCategoryRequestDtoToSubCategory(
                createSubCategoryRequestDto);
        newSubCategory.setCategory(existingCategory);
        SubCategory newSavedSubCategory = subCategoryRepo.save(newSubCategory);

        if (createSubCategoryRequestDto.getFile() != null && !createSubCategoryRequestDto
                .getFile()
                .isEmpty()) {

            MediaResponseDto mediaResponseDto = mediaService.saveSinglemedia(
                    newSavedSubCategory.getId(),
                    EntityType.SUBCATEGORY,
                    createSubCategoryRequestDto.getFile(),
                    createSubCategoryRequestDto.getSubCategoryName());

            newSavedSubCategory.setImageUrl(mediaResponseDto.getUrl());
        }


        // JPA already handles it from the owning side (SubCategory)
//        existingCategory
//                .getSubCategories()
//                .add(savedSubCategory);
//        categoryRepo.save(existingCategory);

        return CommonResponseDto
                .<Void>builder()
                .message("SubCategory created successfully")
                .success(true)
                .status(HttpStatus.CREATED.value())
                .build();
    }

    @Override
    public CommonResponseDto getSubCategoriesWithCategoryId(
            CategoryResponseDto categoryResponseDto) {

        Optional<Category> existingCategoryOptional = categoryRepo.findById(
                categoryResponseDto.getId());

        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();

            List<SubCategory> subCategories = existingCategory.getSubCategories();
            List<SubCategoryResponseDto> subCategoryResponseDtoList = new ArrayList<>();

            subCategories.forEach(subCategory -> {
//                CategoryDto newCategoryDto = new CategoryDto();
//                newCategoryDto.setId(subCategory.getCategory().getId());
//                newCategoryDto.setCategoryName(subCategory.getCategory().getCategoryName());

                subCategoryResponseDtoList.add(SubCategoryResponseDto
                                                       .builder()
                                                       .id(subCategory.getId())
                                                       .categoryId(subCategory
                                                                           .getCategory()
                                                                           .getId())
                                                       .subCategoryName(
                                                               subCategory.getSubCategoryName())
                                                       .createdAt(subCategory.getCreatedAt())
                                                       .updatedAt(subCategory.getUpdatedAt())
                                                       .build());
            });

            return CommonResponseDto
                    .builder()
                    .message("Success")
                    .data(Map.of("results", subCategoryResponseDtoList, "categoryName",
                                 existingCategory.getCategoryName()))
                    .success(true)
                    .build();

        } else {
            throw new ApplicationException("404", "Category not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResponseDto deleteSubCategory(Long subCategoryId) {

        try {
            Optional<SubCategory> existingSubCategory = subCategoryRepo.findById(subCategoryId);

            if (existingSubCategory.isEmpty()) {
                throw new ApplicationException("404", "SubCategory not found",
                                               HttpStatus.NOT_FOUND);
            }

            subCategoryRepo.deleteById(subCategoryId);

            return CommonResponseDto
                    .builder()
                    .message("SubCategory deleted successfully")
                    .data(Map.of("results", existingSubCategory))
                    .success(true)
                    .build();

        } catch (DataAccessException ex) {
            ColorLogger.logError(ex.getMessage());
            throw new ApplicationException("500", ex.getMessage(),
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
