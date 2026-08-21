package com.example.pricetag.features.payment.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.payment.dto.responseDto.InitializePaymentResponseDto;
import com.example.pricetag.features.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderNumber}/payment-intent")
    public ResponseEntity<CommonResponseDto<InitializePaymentResponseDto>> initializePayment(
            @PathVariable
            String orderNumber) {

        return ResponseEntity.ok(paymentService.initializePayment(orderNumber));
    }
}
