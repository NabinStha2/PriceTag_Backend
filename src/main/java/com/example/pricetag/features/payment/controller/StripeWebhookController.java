package com.example.pricetag.features.payment.controller;

import com.example.pricetag.features.payment.service.StripeWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/stripe")
public class StripeWebhookController {
    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {

        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody
            String payload,
            @RequestHeader(value = "Stripe-Signature",
                    required = false)
            String stripeSignature) {
        stripeWebhookService.handleWebhook(payload, stripeSignature);
        return ResponseEntity
                .ok()
                .build();
    }
}
