package com.example.pricetag.features.payment.dto.responseDto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InitializePaymentResponseDto {
    private String clientSecret;

    private String paymentIntentId;

    private String orderNumber;
    /**
     * Amount in Stripe's minor currency unit.
     * For AUD, 13200 means AUD 132.00.
     */
    private Long amount;

    private String currency;

    private LocalDateTime expiresAt;
}
