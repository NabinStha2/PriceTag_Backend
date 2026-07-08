package com.example.pricetag.features.product.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.features.product.dto.request.CreateProductRequestDto;
import com.example.pricetag.features.product.dto.response.ProductResponseDto;
import com.example.pricetag.features.product.dto.response.SingleProductDetailsResponseDto;
import com.example.pricetag.features.product.service.ProductService;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.example.pricetag.utils.ColorLogger;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<CommonResponseDto<List<ProductResponseDto>>> getProductsWithSubCategoryId(
            @PathVariable
            Long subCategoryId,
            @RequestParam(name = "page",
                    defaultValue = "1",
                    required = false
            )
            int page,
            @RequestParam(name = "limit",
                    defaultValue = "5",
                    required = false
            )
            int limit,
            @RequestParam(name = "sortBy",
                    required = false
            )
            String sortBy,
            @RequestParam(name = "order",
                    required = false
            )
            String order) {
        SubCategoryResponseDto subCategoryResponseDto = new SubCategoryResponseDto();
        subCategoryResponseDto.setId(subCategoryId);
        PaginationRequestDto paginationDto = PaginationRequestDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();
//        ColorLogger.logInfo("paginationDto :: " + paginationDto.toString());
        return ResponseEntity.ok(
                productService.getProductsWithSubCategoryId(subCategoryResponseDto, paginationDto));
    }

    @PostMapping("/category/{categoryId}/subcategory/{subCategoryId}/add")
    public ResponseEntity<CommonResponseDto<ProductResponseDto>> createProduct(
            @PathVariable
            Long categoryId,
            @PathVariable
            Long subCategoryId,
            @Valid
            @ModelAttribute
            CreateProductRequestDto createProductRequestDto) {
        createProductRequestDto.setCategoryId(categoryId);
        createProductRequestDto.setSubCategoryId(subCategoryId);
        return ResponseEntity.ok(productService.createProduct(createProductRequestDto));
    }

    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<ProductResponseDto>>> getAllProducts(
            @RequestParam(name = "page",
                    defaultValue = "1",
                    required = false
            )
            int page,
            @RequestParam(name = "limit",
                    defaultValue = "5",
                    required = false
            )
            int limit,
            @RequestParam(name = "sortBy",
                    required = false
            )
            String sortBy,
            @RequestParam(name = "order",
                    required = false
            )
            String order) {
        PaginationRequestDto paginationDto = PaginationRequestDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();
        ColorLogger.logInfo("paginationDto :: " + paginationDto.toString());
        return ResponseEntity.ok(productService.getAllProducts(paginationDto));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponseDto<SingleProductDetailsResponseDto>> getSingleProduct(
            @PathVariable
            Long productId
                                                                                              ) {
        return ResponseEntity.ok(productService.getSingleProduct(productId));
    }

    @GetMapping("/subcategory/{subCategoryId}/search")
    public ResponseEntity<CommonResponseDto<List<ProductResponseDto>>> getSearchProductsWithSubCategoryId(
            @PathVariable
            Long subCategoryId,
            @RequestParam(name = "page",
                    defaultValue = "1",
                    required = false
            )
            int page,
            @RequestParam(name = "limit",
                    defaultValue = "5",
                    required = false
            )
            int limit,
            @RequestParam(name = "sortBy",
                    required = false
            )
            String sortBy,
            @RequestParam(name = "order",
                    required = false
            )
            String order,
            @RequestParam(name = "name",
                    required = false
            )
            String name) {
        SubCategoryResponseDto subCategoryResponseDto = new SubCategoryResponseDto();
        subCategoryResponseDto.setId(subCategoryId);
        PaginationRequestDto paginationDto = PaginationRequestDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();
        ColorLogger.logInfo("paginationDto :: " + paginationDto.toString());
        return ResponseEntity.ok(
                productService.getSearchProductsWithSubCategoryIdAndName(subCategoryResponseDto,
                                                                         paginationDto, name));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteProductById(
            @PathVariable
            Long productId) {
        return ResponseEntity.ok(productService.deleteProductById(productId));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<CommonResponseDto<ProductResponseDto>> editProduct(
            @PathVariable
            Long productId,
            @RequestBody
            CreateProductRequestDto createProductRequestDto) {
        createProductRequestDto.setProductId(productId);
        return ResponseEntity.ok(productService.editProduct(createProductRequestDto));
    }

}
