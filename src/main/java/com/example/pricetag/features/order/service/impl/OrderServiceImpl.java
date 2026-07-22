package com.example.pricetag.features.order.service.impl;

import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.cart.repository.CartItemRepo;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.order.service.OrderService;
import com.example.pricetag.features.product.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepo;
    private final AuthService authService;
    private final ProductRepo productRepo;
    private final CartItemRepo cartItemRepo;

    public OrderServiceImpl(OrderRepo orderRepo, AuthService authService, ProductRepo productRepo,
                            CartItemRepo cartItemRepo) {
        this.orderRepo = orderRepo;
        this.authService = authService;
        this.productRepo = productRepo;
        this.cartItemRepo = cartItemRepo;
    }

//    @Override
//    public CommonResponseDto createOrder() {
//
//        User user = authService.getUser();
//        List<CartItem> filteredCartItemList = new ArrayList<>();
////        for (CartItem cartItem : user.getCartItems()) {
////            if (cartItem.getCheckoutAmt() == null) {
////                filteredCartItemList.add(cartItem);
////            }
////        }
//
//        if (!filteredCartItemList.isEmpty()) {
//            List<CartItem> updatedCartItemList = new ArrayList<>();
////            for (CartItem cartItem : filteredCartItemList) {
//////                if (cartItem.getProduct().getQuantity() < cartItem.getQuantity()) {
//////                    throw new ApplicationException("500", "Product quantity not available", HttpStatus.BAD_REQUEST);
//////                }
//////                cartItem.setCheckoutAmt(cartItem.getProduct().getDiscountedPrice() * cartItem.getQuantity());
////                updatedCartItemList.add(cartItem);
////            }
////            try {
////                cartItemRepo.saveAll(updatedCartItemList);
////
////                orderRepo.save(Order
////                        .builder()
////                        .user(user)
////                        .cartItems(updatedCartItemList)
////                        .totalAmt(updatedCartItemList.stream().mapToDouble(CartItem::getCheckoutAmt).sum())
////                        .build());
////
////                for (CartItem cartItem : updatedCartItemList) {
////                    Product product = cartItem.getProduct();
//////                    product.setQuantity(product.getQuantity() - cartItem.getQuantity());
////                    productRepo.save(product);
////                }
////            } catch (DataAccessException e) {
////                throw new ApplicationException("500", "Database error", HttpStatus.INTERNAL_SERVER_ERROR);
////
////            }
//
//
//        } else {
//            throw new ApplicationException("500", "Cart is empty", HttpStatus.BAD_REQUEST);
//        }
//
//
//        return CommonResponseDto
//                .builder()
//                .build();
//    }
}
