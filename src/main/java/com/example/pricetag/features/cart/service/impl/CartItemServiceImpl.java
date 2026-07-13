package com.example.pricetag.features.cart.service.impl;

import com.example.pricetag.dto.AddCartItemDto;
import com.example.pricetag.dto.CartItemDto;
import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.cart.entity.CartItem;
import com.example.pricetag.features.cart.repository.CartItemRepo;
import com.example.pricetag.features.cart.service.CartItemService;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepo cartItemRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final AuthService authService;
    private final VariantRepo variantRepo;

    public CartItemServiceImpl(CartItemRepo cartItemRepo, UserRepo userRepo,
                               ProductRepo productRepo,
                               AuthService authService, VariantRepo variantRepo) {
        this.cartItemRepo = cartItemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.authService = authService;
        this.variantRepo = variantRepo;
    }

    private static CartItemDto buildCartItemDto(CartItem newCartItem, User existingUser) {
//        Product product = newCartItem.getProduct();

        return CartItemDto
                .builder()
                .id(newCartItem.getId())
//                .product(CreateProductRequestDto
//                                 .builder()
//                                 .name(product != null ? product.getName() : null)
//                                 .description(product != null ? product.getDescription() : null)
////                        .images(product != null ? product.getImages() : null)
//                                 .productId(product != null ? product.getId() : null)
////                                 .createdAt(product != null ? product.getCreatedAt() : null)
                ////                                 .updatedAt(product != null ? product.getUpdatedAt() : null)
//                                 .category(product != null && product.getCategory() != null ?
//                                           CategoryResponseDto
//                                           .builder()
//                                           .id(product
//                                               .getCategory()
//                                               .getId())
//                                           .name(product
//                                                 .getCategory()
//                                                 .getCategoryName())
//                                           .build() : null)
//                                 .subCategory(product != null && product.getSubCategory() != null ?
//                                              SubCategoryDto
//                                              .builder()
//                                              .id(product
//                                                  .getSubCategory()
//                                                  .getId())
//                                              .subCategoryName(product
//                                                               .getSubCategory()
//                                                               .getSubCategoryName())
//                                              .build() : null)
//                                 .build())
                .user(existingUser)
                .quantity(newCartItem.getQuantity())
                .createdAt(newCartItem.getCreatedAt())
                .updatedAt(newCartItem.getUpdatedAt())
                .checkoutAmt(newCartItem.getTotalPrice())
                .variants(newCartItem.getVariant())
                .build();
    }

    @Override
    public CommonResponseDto getCart() {
        User existingUser = authService.getUser();

        List<CartItemDto> listOfCartItemDto = new ArrayList<>();

        existingUser
                .getCart()
                .getCartItems()
                .stream()
                .sorted(Comparator
                                .comparing(CartItem::getCreatedAt)
                                .reversed())
                .toList()
                .forEach(cartItem -> {
                    if (cartItem.getTotalPrice() == null) {
                        listOfCartItemDto.add(buildCartItemDto(cartItem, existingUser));
                    }
                });

        return CommonResponseDto
                .builder()
                .message("Cart fetched successfully")
                .data(Map.of("results", listOfCartItemDto))
                .success(true)
                .build();

    }


    @Override
    public CommonResponseDto createCart(AddCartItemDto addCartItemDto) {
        // Get the current user
        User existingUser = authService.getUser();

        // Log user information
        ColorLogger.logInfo("I am inside getCart :: " + existingUser);

        // Check if the product exists
        Optional<Product> existingProduct = productRepo.findById(addCartItemDto.getProductId());
        if (existingProduct.isPresent()) {
            // Check if the variant exists within the product
            Optional<Variants> variantPresent = existingProduct
                    .get()
                    .getVariants()
                    .stream()
                    .filter(variant -> variant
                            .getId()
                            .equals(addCartItemDto.getVariantId()))
                    .findFirst();
            if (variantPresent.isEmpty()) {
                throw new ApplicationException("404", "Variant not found inside given product id",
                                               HttpStatus.NOT_FOUND);
            }

            // Check if the variant exists
            Optional<Variants> existingVariant = variantRepo.findById(
                    addCartItemDto.getVariantId());
            if (existingVariant.isEmpty()) {
                throw new ApplicationException("404", "Variant not found", HttpStatus.NOT_FOUND);
            }

            // Check if the requested quantity is available
            if (addCartItemDto.getQuantity() > existingVariant
                    .get()
                    .getStockQuantity()) {
                throw new ApplicationException("400", "Product quantity not available",
                                               HttpStatus.BAD_REQUEST);
            }

            // Get the list of existing cart items for the user
            List<CartItem> listOfCartItem = existingUser
                    .getCart()
                    .getCartItems();

            // Check if the cart item already exists
            Optional<CartItem> filteredCartItem = listOfCartItem
                    .stream()
                    .filter(cartItem -> cartItem
                                                .getId()
                                                .equals(existingProduct
                                                                .get()
                                                                .getId())
                                        && cartItem.getTotalPrice() == null
                                        && cartItem
                                                .getVariant()
                                                .getId()
                                                .equals(existingVariant
                                                                .get()
                                                                .getId()))
                    .findFirst();

            if (filteredCartItem.isPresent()) {
                // Update existing cart item quantity
                CartItem existingCartItem = filteredCartItem.get();
                existingCartItem.setQuantity(addCartItemDto.getQuantity());
                cartItemRepo.save(existingCartItem);
                return CommonResponseDto
                        .builder()
                        .message("Product added to cart successfully")
                        .data(Map.of("results", buildCartItemDto(existingCartItem, existingUser)))
                        .success(true)
                        .build();
            } else {
                // Create a new cart item
                CartItem newCartItem = CartItem
                        .builder()
                        .quantity(addCartItemDto.getQuantity())
                        .variant(existingVariant.get())
                        .build();
                CartItem savedCartItem = cartItemRepo.save(newCartItem);

                // Update user cart items and save user
                existingUser
                        .getCart()
                        .getCartItems()
                        .add(savedCartItem);
                userRepo.save(existingUser);

                // Return success response
                return CommonResponseDto
                        .builder()
                        .message("Product created to cart successfully")
                        .data(Map.of("results", buildCartItemDto(newCartItem, existingUser)))
                        .success(true)
                        .build();
            }

        } else {
            // Product not found
            throw new ApplicationException("404", "Product not found", HttpStatus.NOT_FOUND);
        }
    }

    public CommonResponseDto deleteCartItem(Long cartItemId) {
        User existingUser = authService.getUser();

        List<CartItem> listOfCartItems = existingUser
                .getCart()
                .getCartItems();

        Optional<CartItem> filteredCartItem = existingUser
                .getCart()
                .getCartItems()
                .stream()
                .filter(cartItem -> {
                    return cartItem
                            .getId()
                            .equals(cartItemId);
                })
                .findFirst();

        if (filteredCartItem.isPresent()) {
            existingUser
                    .getCart()
                    .getCartItems()
                    .remove(filteredCartItem.get());
            cartItemRepo.delete(filteredCartItem.get());
            userRepo.save(existingUser);

            return CommonResponseDto
                    .builder()
                    .message("Cart item deleted successfully")
                    .data(Map.of("results", filteredCartItem.get()))
                    .success(true)
                    .build();
        } else {
            throw new ApplicationException("404",
                                           "Delete of cart item id doesn't present inside user cart",
                                           HttpStatus.NOT_FOUND);
        }
    }


}
