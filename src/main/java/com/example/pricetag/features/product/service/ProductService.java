package com.example.pricetag.features.product.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.features.product.dto.request.CreateProductRequestDto;
import com.example.pricetag.features.product.dto.response.ProductResponseDto;
import com.example.pricetag.features.product.dto.response.SingleProductDetailsResponseDto;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;

import java.util.List;

public interface ProductService {

    CommonResponseDto<ProductResponseDto> createProduct(
            CreateProductRequestDto createProductRequestDto);

    CommonResponseDto<List<ProductResponseDto>> getAllProducts(
            PaginationRequestDto paginationRequestDto);

    CommonResponseDto<SingleProductDetailsResponseDto> getSingleProduct(Long productId);

    CommonResponseDto<List<ProductResponseDto>> getProductsWithSubCategoryId(
            SubCategoryResponseDto subCategoryResponseDto,
            PaginationRequestDto paginationRequestDto);

    CommonResponseDto<List<ProductResponseDto>> getSearchProductsWithSubCategoryIdAndName(
            SubCategoryResponseDto subCategoryResponseDto,
            PaginationRequestDto paginationRequestDto,
            String name);

    CommonResponseDto<Void> deleteProductById(Long productId);

    CommonResponseDto<ProductResponseDto> editProduct(
            CreateProductRequestDto createProductRequestDto);

}
