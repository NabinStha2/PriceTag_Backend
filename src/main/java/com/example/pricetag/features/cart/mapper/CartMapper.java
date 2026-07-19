package com.example.pricetag.features.cart.mapper;


import com.example.pricetag.features.cart.dto.responseDto.CartItemResponseDto;
import com.example.pricetag.features.cart.dto.responseDto.CartResponseDto;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CartMapper {
    @Mapping(target = "cartId",
            source = "id")
    @Mapping(target = "items",
            source = "cartItems")
    CartResponseDto cartToCartResponseDto(Cart cart);

    @Mapping(target = "itemId",
            source = "id")
    @Mapping(target = "variantId",
            source = "variant.id")
    @Mapping(target = "sku",
            source = "variant.sku")
    @Mapping(target = "productName",
            source = "variant.product.name")
    @Mapping(target = "imageUrl",
            source = "variant.product.primaryImageUrl")
    @Mapping(target = "colorName",
            source = "variant.color.name")
    @Mapping(target = "colorHex",
            source = "variant.color.hexCode")
    @Mapping(target = "sizeName",
            source = "variant.size.name")
    @Mapping(target = "stockAvailable",
            source = "variant.stockQuantity")
    @Mapping(target = "unitPrice",
            source = "unitPrice")
    @Mapping(target = "lineTotal",
            source = "totalPrice")
    CartItemResponseDto cartItemToCartItemResponseDto(CartItem cartItem);
}
