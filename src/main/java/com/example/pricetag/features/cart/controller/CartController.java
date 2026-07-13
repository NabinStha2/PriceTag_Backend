package com.example.pricetag.features.cart.controller;


import com.example.pricetag.dto.CommonResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    @PostMapping("/items")
    public ResponseEntity<CommonResponseDto<Void>> createCart() {
        return ResponseEntity.ok(null);
    }
}
