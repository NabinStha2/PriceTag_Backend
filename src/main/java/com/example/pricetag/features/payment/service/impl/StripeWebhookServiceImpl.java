package com.example.pricetag.features.payment.service.impl;

import com.example.pricetag.features.payment.service.StripeWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@Transactional
public class StripeWebhookServiceImpl implements StripeWebhookService {
    private static final String STRIPE_PROVIDER = "STRIPE";

    private static final Set<String> SUPPORTED_EVENTS = Set.of("payment_intent.succeeded",
                                                               "payment_intent.payment_failed");

    private final String webhookSecret;

    public StripeWebhookServiceImpl(
            @Value("${stripe.webhook-secret}")
            String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @Override
    public void handleWebhook(String payload, String stripeSignature) {
        // Implementation for handling webhook
    }
}
