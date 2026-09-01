package com.example.pricetag.features.product.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginatedResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.enums.EntityType;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.dto.requestDto.VariantRequestDto;
import com.example.pricetag.features.Variant.entity.Color;
import com.example.pricetag.features.Variant.entity.Size;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.ColorRepo;
import com.example.pricetag.features.Variant.repository.SizeRepo;
import com.example.pricetag.features.Variant.service.VariantService;
import com.example.pricetag.features.category.entity.Category;
import com.example.pricetag.features.category.repository.CategoryRepo;
import com.example.pricetag.features.media.dto.response.MediaResponseDto;
import com.example.pricetag.features.media.service.MediaService;
import com.example.pricetag.features.product.dto.request.CreateProductRequestDto;
import com.example.pricetag.features.product.dto.response.ProductResponseDto;
import com.example.pricetag.features.product.dto.response.SingleProductDetailsResponseDto;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.mapper.ProductMapper;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.product.service.ProductService;
import com.example.pricetag.features.subcategory.dto.response.SubCategoryResponseDto;
import com.example.pricetag.features.subcategory.entity.SubCategory;
import com.example.pricetag.features.subcategory.repository.SubCategoryRepo;
import com.example.pricetag.utils.ColorLogger;
import com.example.pricetag.utils.PageableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final SubCategoryRepo subCategoryRepo;
    private final ProductMapper productMapper;
    private final MediaService mediaService;
    private final VariantService variantService;
    private final SizeRepo sizeRepo;
    private final ColorRepo colorRepo;

    private static Product createNewProduct(CreateProductRequestDto createProductRequestDto,
                                            SubCategory filteredSubCategory, Category category,
                                            String slug) {
        Product newProduct = new Product();
        newProduct.setCategory(category);
        newProduct.setSubCategory(filteredSubCategory);
        newProduct.setName(createProductRequestDto.getName());
        newProduct.setSlug(slug);
        newProduct.setDescription(createProductRequestDto.getDescription());
        newProduct.setShortDescription(createProductRequestDto.getShortDescription());
        newProduct.setBrand(createProductRequestDto.getBrand());
        newProduct.setBasePrice(createProductRequestDto.getBasePrice());
        newProduct.setDiscountedPrice(createProductRequestDto.getDiscountedPrice());

        //        if (productDto.getVariants() != null) {
//            productDto
//                    .getVariants()
//                    .forEach(variant -> variant.setProduct(newProduct));
//            newProduct.setVariants(productDto.getVariants());
//        }

        return newProduct;
    }

    private static String createSlug(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

//    private static void validateCreateProduct(CreateProductRequestDto createProductRequestDto) {
//        if (createProductRequestDto.getName() == null || createProductRequestDto
//                .getName()
//                .isBlank()) {
//            throw new ApplicationException("400", "Product name is required",
//                                           HttpStatus.BAD_REQUEST);
//        }
//
//        if (createProductRequestDto.getDescription() == null || createProductRequestDto
//                .getDescription()
//                .isBlank()) {
//            throw new ApplicationException("400", "Product description is required",
//                                           HttpStatus.BAD_REQUEST);
//        }
//
//        if (createProductRequestDto.getBasePrice() == null) {
//            throw new ApplicationException("400", "Product base price is required",
//                                           HttpStatus.BAD_REQUEST);
//        }
//    }

    private String buildUniqueSlug(String name) {
        String baseSlug = createSlug(name);
        if (baseSlug.isBlank()) {
            baseSlug = "product";
        }

        String slug = baseSlug;
        int counter = 1;

        while (productRepo.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String resolveSlug(CreateProductRequestDto createProductRequestDto) {
        if (createProductRequestDto.getSlug() == null || createProductRequestDto
                .getSlug()
                .isBlank()) {
            return buildUniqueSlug(createProductRequestDto.getName());
        }

        String slug = createSlug(createProductRequestDto.getSlug());
        if (slug.isBlank()) {
            throw new ApplicationException("400", "Product slug is invalid",
                                           HttpStatus.BAD_REQUEST);
        }

        if (productRepo.existsBySlug(slug)) {
            throw new ApplicationException("409", "Product slug already exists",
                                           HttpStatus.CONFLICT);
        }

        return slug;
    }


    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<SingleProductDetailsResponseDto> getSingleProduct(Long productId) {
        Product product = productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));
        SingleProductDetailsResponseDto productDetailsResponseDto = productMapper.mapProductToSingleProductDetailsResponseDto(
                product);

        List<MediaResponseDto> mediaDtos = mediaService.getmedias(productId, EntityType.PRODUCT);
        productDetailsResponseDto.setImageUrl(mediaDtos);

        return CommonResponseDto
                .<SingleProductDetailsResponseDto>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product fetched successfully")
                .data(productDetailsResponseDto)
                .build();
    }


    @Override
    public CommonResponseDto<ProductResponseDto> editProduct(
            CreateProductRequestDto createProductRequestDto) {
        Product existingProduct = productRepo
                .findByIdAndIsActiveTrue(createProductRequestDto.getProductId())
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));

        existingProduct.setName(createProductRequestDto.getName());
        existingProduct.setDescription(createProductRequestDto.getDescription());

        if (createProductRequestDto.getCategoryId() != null) {
            existingProduct.setCategory(categoryRepo
                                                .findById(createProductRequestDto.getCategoryId())
                                                .orElseThrow(() -> new ApplicationException("404",
                                                                                            "Category not found",
                                                                                            HttpStatus.NOT_FOUND)));
        }
        if (createProductRequestDto.getSubCategoryId() != null) {
            existingProduct.setSubCategory(subCategoryRepo
                                                   .findById(
                                                           createProductRequestDto.getSubCategoryId())
                                                   .orElseThrow(
                                                           () -> new ApplicationException("404",
                                                                                          "Sub Category not found",
                                                                                          HttpStatus.NOT_FOUND)));
        }

        try {
            productRepo.save(existingProduct);
            return CommonResponseDto
                    .<ProductResponseDto>builder()
                    .message("Product has been updated successfully")
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .build();
        } catch (DataAccessException e) {
            ColorLogger.logError("editProduct :: Database error :: " + e.getMessage());
            throw new ApplicationException("500", "Database error",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public CommonResponseDto<ProductResponseDto> createProduct(
            CreateProductRequestDto createProductRequestDto) {
        Long categoryId = createProductRequestDto.getCategoryId();
        Long subCategoryId = createProductRequestDto.getSubCategoryId();

//        validateCreateProduct(createProductRequestDto);

        Category existingCategory = categoryRepo
                .findById(categoryId)
                .orElseThrow(() -> new ApplicationException("404", "Category not found",
                                                            HttpStatus.NOT_FOUND));

        SubCategory existingSubCategory = subCategoryRepo
                .findByIdAndCategoryId(subCategoryId, categoryId)
                .orElseThrow(() -> new ApplicationException("404",
                                                            "Sub Category not found in the specified category",
                                                            HttpStatus.NOT_FOUND));

        String slug = resolveSlug(createProductRequestDto);
        Product newProduct = createNewProduct(createProductRequestDto, existingSubCategory,
                                              existingCategory, slug);

        // Handle variants (if provided) - build variant entities and attach to the new product
        if (createProductRequestDto.getVariants() != null && !createProductRequestDto
                .getVariants()
                .isEmpty()) {
            for (VariantRequestDto vDto : createProductRequestDto.getVariants()) {
                Size size = null;
                Color color = null;

                if (vDto.getSizeId() != null) {
                    size = sizeRepo
                            .findById(vDto.getSizeId())
                            .orElseThrow(() -> new ApplicationException("404", "Size not found",
                                                                        HttpStatus.NOT_FOUND));
                }

                if (vDto.getColorId() != null) {
                    color = colorRepo
                            .findById(vDto.getColorId())
                            .orElseThrow(() -> new ApplicationException("404", "Color not found",
                                                                        HttpStatus.NOT_FOUND));
                }

                Variants variant = Variants
                        .builder()
                        .sku(vDto.getSku())
                        .stockQuantity(
                                vDto.getStockQuantity() == null ? 0 : vDto.getStockQuantity())
                        .actualPrice(vDto.getActualPrice() == null ?
                                     createProductRequestDto.getBasePrice() : vDto.getActualPrice())
                        .discountedPrice(vDto.getDiscountedPrice())
                        .weightInGrams(vDto.getWeightInGrams())
                        .size(size)
                        .color(color)
                        .product(newProduct)
                        .build();

                newProduct
                        .getVariants()
                        .add(variant);
            }
        }

        Product savedProduct = productRepo.save(newProduct);

        // Attach images (if any) after product is saved
        mediaService.saveMultimedias(savedProduct.getId(), EntityType.PRODUCT,
                                     createProductRequestDto.getImages(), savedProduct
                                             .getId()
                                             .toString());

        // Safely set primary image URL if available
        List<MediaResponseDto> medias = mediaService.getmedias(savedProduct.getId(),
                                                               EntityType.PRODUCT);
        if (medias != null && !medias.isEmpty()) {
            savedProduct.setPrimaryImageUrl(medias
                                                    .getFirst()
                                                    .getUrl());
        }

        ProductResponseDto productResponseDto = productMapper.mapProductToProductResponseDto(
                savedProduct);

        return CommonResponseDto
                .<ProductResponseDto>builder()
                .data(productResponseDto)
                .message("Product has been created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<ProductResponseDto>> getProductsWithSubCategoryId(
            SubCategoryResponseDto subCategoryResponseDto,
            PaginationRequestDto paginationRequestDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationRequestDto);
        if (subCategoryRepo.existsSubCategoryById(subCategoryResponseDto.getId())) {
            Page<Product> existingProducts = productRepo.findAllBySubCategoryIdAndIsActiveTrue(
                    subCategoryResponseDto.getId(), pageable);
            List<ProductResponseDto> productResponseDtoList = productMapper.mapProductListToProductResponseDtoList(
                    existingProducts.getContent());

            return CommonResponseDto
                    .<List<ProductResponseDto>>builder()
                    .message("Products fetch Successfully")
                    .data(productResponseDtoList)
                    .pagination(PaginatedResponseDto.from(existingProducts))
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .build();
        } else {
            throw new ApplicationException("404", "Sub Category not found", HttpStatus.NOT_FOUND);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<ProductResponseDto>> getAllProducts(
            PaginationRequestDto paginationRequestDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationRequestDto);

        Page<Product> existingProductList = productRepo.findAllByIsActiveTrue(pageable);

        List<ProductResponseDto> productResponseDtoList = productMapper.mapProductListToProductResponseDtoList(
                existingProductList.getContent());

        return CommonResponseDto
                .<List<ProductResponseDto>>builder()
                .message("Products fetched successfully")
                .data(productResponseDtoList)
                .pagination(PaginatedResponseDto.from(existingProductList))
                .success(true)
                .status(HttpStatus.OK.value())
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<ProductResponseDto>> getSearchProductsWithSubCategoryIdAndName(
            SubCategoryResponseDto subCategoryResponseDto,
            PaginationRequestDto paginationRequestDto, String name) {
        Pageable pageable = PageableBuilder.buildPageable(paginationRequestDto);
        if (subCategoryRepo.existsSubCategoryById(subCategoryResponseDto.getId())) {
            Page<Product> productsList = productRepo.findAllBySubCategoryIdAndNameContainingIgnoreCase(
                    subCategoryResponseDto.getId(), pageable, name);

            List<ProductResponseDto> productResponseDtoList = productMapper.mapProductListToProductResponseDtoList(
                    productsList.getContent());

            return CommonResponseDto
                    .<List<ProductResponseDto>>builder()
                    .message("Search products fetch Successfully")
                    .data(productResponseDtoList)
                    .pagination(PaginatedResponseDto.from(productsList))
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .build();
        } else {
            throw new ApplicationException("404", "Sub Category not found", HttpStatus.NOT_FOUND);
        }
    }


    @Override
    public CommonResponseDto<Void> deleteProductById(Long productId) {
        Product existingProduct = productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));

//            product.getImages()
//                    .forEach(image -> cloudinaryService.deleteFile(image.getPublicId(), "pricetag/" + product.getCategory()
//                            .getCategoryName() + "/" + product.getSubCategory().getSubCategoryName()));

        existingProduct.setIsActive(false);
        existingProduct.setIsDeleted(true);
        productRepo.save(existingProduct);

        return CommonResponseDto
                .<Void>builder()
                .message("Product has been deleted successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();
    }


}
