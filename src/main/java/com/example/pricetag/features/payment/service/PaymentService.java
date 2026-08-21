package com.example.pricetag.features.payment.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.payment.dto.responseDto.InitializePaymentResponseDto;

public interface PaymentService {

    CommonResponseDto<InitializePaymentResponseDto> initializePayment(String orderNumber);
}
