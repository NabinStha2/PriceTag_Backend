package com.example.pricetag.services.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.entity.CartItem;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.repository.CartItemRepo;
import com.example.pricetag.repository.OrderRepo;
import com.example.pricetag.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private AuthService authService;

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    @Override
    public CommonResponseDto createOrder() {

        User user = authService.getUser();
        List<CartItem> filteredCartItemList = new ArrayList<>();
//        for (CartItem cartItem : user.getCartItems()) {
//            if (cartItem.getCheckoutAmt() == null) {
//                filteredCartItemList.add(cartItem);
//            }
//        }

        if (!filteredCartItemList.isEmpty()) {
            List<CartItem> updatedCartItemList = new ArrayList<>();
//            for (CartItem cartItem : filteredCartItemList) {
////                if (cartItem.getProduct().getQuantity() < cartItem.getQuantity()) {
////                    throw new ApplicationException("500", "Product quantity not available", HttpStatus.BAD_REQUEST);
////                }
////                cartItem.setCheckoutAmt(cartItem.getProduct().getDiscountedPrice() * cartItem.getQuantity());
//                updatedCartItemList.add(cartItem);
//            }
//            try {
//                cartItemRepo.saveAll(updatedCartItemList);
//
//                orderRepo.save(Order
//                        .builder()
//                        .user(user)
//                        .cartItems(updatedCartItemList)
//                        .totalAmt(updatedCartItemList.stream().mapToDouble(CartItem::getCheckoutAmt).sum())
//                        .build());
//
//                for (CartItem cartItem : updatedCartItemList) {
//                    Product product = cartItem.getProduct();
////                    product.setQuantity(product.getQuantity() - cartItem.getQuantity());
//                    productRepo.save(product);
//                }
//            } catch (DataAccessException e) {
//                throw new ApplicationException("500", "Database error", HttpStatus.INTERNAL_SERVER_ERROR);
//
//            }


        } else {
            throw new ApplicationException("500", "Cart is empty", HttpStatus.BAD_REQUEST);
        }


        return CommonResponseDto
                .builder()
                .build();
    }
}
