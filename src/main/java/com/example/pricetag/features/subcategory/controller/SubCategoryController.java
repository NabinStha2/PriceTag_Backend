package com.example.pricetag.features.subcategory.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.category.dto.response.CategoryResponseDto;
import com.example.pricetag.features.subcategory.dto.request.CreateSubCategoryRequestDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.example.pricetag.features.subcategory.service.SubCategoryService;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subcategory")
public class SubCategoryController {

    @Autowired
    private SubCategoryService subCategoryService;

    @GetMapping("")
    public ResponseEntity<CommonResponseDto> getAllSubCategories() throws ApplicationException {
        return ResponseEntity.ok(subCategoryService.getAllSubCategories());
    }

    @PatchMapping("/edit")
    public ResponseEntity<CommonResponseDto> editSubCategory(
            @RequestBody
            SubCategoryResponseDto subCategoryResponseDto)
            throws ApplicationException {
        ColorLogger.logInfo(subCategoryResponseDto.toString());
        return ResponseEntity.ok(subCategoryService.editSubCategory(subCategoryResponseDto));
    }

    @PostMapping("/{categoryId}/add")
    public ResponseEntity<CommonResponseDto<Void>> createSubCategory(
            @PathVariable(name = "categoryId")
            Long categoryId,
            @ModelAttribute
            CreateSubCategoryRequestDto createSubCategoryRequestDto)
            throws ApplicationException {
        return ResponseEntity.ok(
                subCategoryService.createSubCategory(categoryId, createSubCategoryRequestDto));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CommonResponseDto> getSubCategoriesWithCategoryId(
            @PathVariable(name = "categoryId")
            Long categoryId) throws ApplicationException {
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(categoryId);
        return ResponseEntity.ok(
                subCategoryService.getSubCategoriesWithCategoryId(categoryResponseDto));
    }

    @DeleteMapping("/{subCategoryId}")
    public ResponseEntity<CommonResponseDto> deleteCategory(
            @PathVariable(name = "subCategoryId")
            Long subCategoryId)
            throws ApplicationException {
        return ResponseEntity.ok(subCategoryService.deleteSubCategory(subCategoryId));
    }


}
