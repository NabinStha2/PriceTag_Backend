package com.example.pricetag.features.payment.service;

import com.example.pricetag.exceptions.ApplicationException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripePaymentIntentService {

    private final StripeClient stripeClient;

    public StripePaymentIntentService(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    public void cancel(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new ApplicationException("PAYMENT_REFERENCE_MISSING",
                                           "Cannot cancel payment without a payment reference",
                                           HttpStatus.CONFLICT);
        }

        try {
            PaymentIntent paymentIntent = stripeClient
                    .v1()
                    .paymentIntents()
                    .retrieve(paymentIntentId);

            if ("canceled".equals(paymentIntent.getStatus())) {
                return;
            }
            if ("succeeded".equals(paymentIntent.getStatus())) {
                throw new ApplicationException("PAYMENT_ALREADY_SUCCEEDED",
                                               "The Stripe payment has already succeeded",
                                               HttpStatus.CONFLICT);
            }

            PaymentIntent cancelledIntent = stripeClient
                    .v1()
                    .paymentIntents()
                    .cancel(paymentIntentId);

            if (!"canceled".equals(cancelledIntent.getStatus())) {
                throw new ApplicationException("PAYMENT_CANCELLATION_UNCONFIRMED",
                                               "Payment cancellation could not be confirmed",
                                               HttpStatus.CONFLICT);
            }

        } catch (StripeException exception) {
            log.error("Unable to cancel Stripe PaymentIntent {}: {}", paymentIntentId,
                      exception.getMessage());

            throw new ApplicationException("STRIPE_CANCELLATION_ERROR",
                                           "Unable to cancel the Stripe payment. Please try again.",
                                           HttpStatus.BAD_GATEWAY);
        }
    }
}
