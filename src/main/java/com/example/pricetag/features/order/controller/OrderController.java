package com.example.pricetag.features.order.controller;

import com.example.pricetag.features.order.service.OrderService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

//    @PostMapping("/add")
//    public ResponseEntity<CommonResponseDto<Void>> createOrder() {
//        return new ResponseEntity<CommonResponseDto<Void>>(orderService.createOrder(),
//                                                           HttpStatus.CREATED);
//    }
}
