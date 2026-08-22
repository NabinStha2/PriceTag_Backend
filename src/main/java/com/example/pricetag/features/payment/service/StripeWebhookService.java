package com.example.pricetag.features.payment.service;

public interface StripeWebhookService {

    void handleWebhook(String payload, String stripeSignature);
}
