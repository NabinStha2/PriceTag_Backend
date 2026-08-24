package com.example.pricetag.features.payment.service.impl;

import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.entity.OrderStatusHistory;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.payment.entity.Payment;
import com.example.pricetag.features.payment.repository.PaymentRepo;
import com.example.pricetag.features.payment.service.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class StripeWebhookServiceImpl implements StripeWebhookService {
    private static final String STRIPE_PROVIDER = "STRIPE";

    private static final Set<String> SUPPORTED_EVENTS = Set.of("payment_intent.succeeded",
                                                               "payment_intent.payment_failed");

    private final String webhookSecret;
    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final CartRepo cartRepo;

    public StripeWebhookServiceImpl(
            @Value("${stripe.webhook-secret}")
            String webhookSecret, OrderRepo orderRepo, PaymentRepo paymentRepo, CartRepo cartRepo) {
        this.webhookSecret = webhookSecret;
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.cartRepo = cartRepo;
    }


    /*
    payments.payment_status = SUCCEEDED
    orders.payment_status   = SUCCEEDED
    orders.order_status     = CONFIRMED
    orders.paid_at          = current time
    carts.status            = ACTIVE
    cart_items              = cleared
     */
    @Override
    public void handleWebhook(String payload, String stripeSignature) {
 /*
           * Step 1: Verify that this request genuinely came from
           Stripe.
           */
        Event event = verifyWebhook(payload, stripeSignature);

        /*
         * Stripe sends many event types. We only need these two for now.
         * Returning normally causes the controller to return HTTP 200.
         */
        if (!SUPPORTED_EVENTS.contains(event.getType())) {
            log.debug("Ignoring Stripe event type: {}", event.getType());
            return;
        }

        /*
         * Step 2: Extract the PaymentIntent from the event.
         */
        PaymentIntent paymentIntent = extractPaymentIntent(event);

        Long orderId = paymentRepo
                .findOrderIdByPaymentId(paymentIntent.getId())
                .orElseThrow(this::paymentNotFound);

        /* Lock order before payment everywhere to avoid deadlocks. */
        orderRepo
                .findByIdForUpdate(orderId)
                .orElseThrow(this::paymentNotFound);

        Payment payment = paymentRepo
                .findByPaymentIdForUpdate(paymentIntent.getId())
                .orElseThrow(this::paymentNotFound);

        /*
         * Step 4: Process the event.
         */
        switch (event.getType()) {
            case "payment_intent.succeeded" ->
                    handlePaymentSucceeded(payment, paymentIntent, event.getId());

            case "payment_intent.payment_failed" -> handlePaymentFailed(payment, event.getId());

            default -> {
                // Already filtered above.
            }
        }

    }

    // ========================================================================================
    // HELPER METHODS
    // ========================================================================================

    private ApplicationException paymentNotFound() {
        return new ApplicationException("PAYMENT_NOT_FOUND", "Payment record was not found",
                                        HttpStatus.NOT_FOUND);
    }

    private Event verifyWebhook(String payload, String stripeSignature) {
        if (stripeSignature == null || stripeSignature.isBlank()) {
            throw new ApplicationException("MISSING_STRIPE_SIGNATURE",
                                           "Stripe-Signature header is required",
                                           HttpStatus.BAD_REQUEST);
        }

        try {
            return Webhook.constructEvent(payload, stripeSignature, webhookSecret);
        } catch (SignatureVerificationException exception) {
            log.warn("Stripe signature verification failed: {}", exception.getMessage());
            throw new ApplicationException("INVALID_STRIPE_WEBHOOK", "Invalid Stripe webhook",
                                           HttpStatus.BAD_REQUEST);
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        StripeObject stripeObject = event
                .getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new ApplicationException("INVALID_STRIPE_EVENT",
                                                            "Stripe event could not be deserialized",
                                                            HttpStatus.BAD_REQUEST));

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            throw new ApplicationException("INVALID_STRIPE_EVENT_OBJECT",
                                           "Stripe event does not contain a PaymentIntent",
                                           HttpStatus.BAD_REQUEST);
        }

        return paymentIntent;
    }

    private void handlePaymentSucceeded(Payment payment, PaymentIntent paymentIntent,
                                        String eventId) {

        Order order = payment.getOrder();

        /*
         * Stripe can retry the same webhook.
         * If already processed, don't repeat cart/history changes.
         */
        if (payment.getPaymentStatus() == PaymentStatus.SUCCEEDED &&
            order.getPaymentStatus() == PaymentStatus.SUCCEEDED) {

            log.info("Payment {} was already processed", paymentIntent.getId());
            return;
        }

        validateSuccessfulPayment(payment, paymentIntent, order);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApplicationException("INVALID_ORDER_STATUS",
                                           "Order is not waiting for payment", HttpStatus.CONFLICT);
        }

        OrderStatus previousStatus = order.getStatus();

        /*
         * Update the payment.
         */
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);

        /*
         * Update the order.
         */
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentReference(paymentIntent.getId());

        /*
         * Add audit history.
         */
        order.addStatusHistory(OrderStatusHistory
                                       .builder()
                                       .previousStatus(previousStatus)
                                       .newStatus(OrderStatus.CONFIRMED)
                                       .note("Payment confirmed by Stripe. Event:" + eventId)
                                       .actorUserId(order
                                                            .getUser()
                                                            .getId())
                                       .actorEmail(order
                                                           .getUser()
                                                           .getEmail())
                                       .build());

        /*
         * Order items are already stored as snapshots.
         * The successfully paid cart can now be cleared.
         */
        cartRepo
                .findByUserIdForCheckout(order
                                                 .getUser()
                                                 .getId())
                .ifPresent(cart -> {
                    cart
                            .getCartItems()
                            .clear();
                    cart.setStatus(CartStatus.ACTIVE);
                });

        /*
         * Explicit save calls aren't required because Payment, Order
         * and Cart are managed inside this transaction.
         */
        log.info("Order {} confirmed successfully", order.getOrderNumber());
    }

    private void handlePaymentFailed(Payment payment, String eventId) {
        /*
         * Never allow an older failed event to downgrade a payment
         * that has already succeeded.
         */
        if (payment.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Ignoring failed event {} because payment already succeeded", eventId);
            return;
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

          /*
           * Keep Order.paymentStatus as PENDING.
           *
           * The customer can retry the same Stripe PaymentIntent
           while
           * the order remains within its 15-minute payment window.
           */
        log.info("Stripe payment attempt failed for order {}", payment
                .getOrder()
                .getOrderNumber());
    }

    private void validateSuccessfulPayment(Payment payment, PaymentIntent paymentIntent,
                                           Order order) {
        if (!STRIPE_PROVIDER.equals(payment.getPaymentProvider())) {
            throw new ApplicationException("INVALID_PAYMENT_PROVIDER",
                                           "Payment is not a Stripe payment", HttpStatus.CONFLICT);
        }

        long expectedAmount = order
                .getTotalAmt()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        if (paymentIntent.getAmount() == null || paymentIntent.getAmount() != expectedAmount) {

            throw new ApplicationException("PAYMENT_AMOUNT_MISMATCH",
                                           "Stripe amount does not match the order amount",
                                           HttpStatus.CONFLICT);
        }

        if (paymentIntent.getCurrency() == null || !paymentIntent
                .getCurrency()
                .equalsIgnoreCase(order.getCurrency())) {

            throw new ApplicationException("PAYMENT_CURRENCY_MISMATCH",
                                           "Stripe currency does not match the order currency",
                                           HttpStatus.CONFLICT);
        }
    }

}
