package com.example.pricetag.features.cart.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {

    private Long cartId;

    // "ACTIVE" / "CHECKOUT" / "ABANDONED" - lets Android disable
    // edit controls (quantity steppers, remove buttons) when the cart
    // is locked mid-checkout, instead of letting the user tap them and
    // get a 409 back.
    private String status;

    private List<CartItemResponseDto> items;

    // Computed in CartServiceImpl by summing item lineTotals - never
    // persisted on Cart itself.
    private BigDecimal cartTotal;
    private Integer totalItemCount;
}