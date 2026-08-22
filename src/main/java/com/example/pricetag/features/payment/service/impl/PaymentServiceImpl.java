package com.example.pricetag.features.payment.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentMethod;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.payment.dto.responseDto.InitializePaymentResponseDto;
import com.example.pricetag.features.payment.entity.Payment;
import com.example.pricetag.features.payment.repository.PaymentRepo;
import com.example.pricetag.features.payment.service.PaymentService;
import com.example.pricetag.features.user.entity.User;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private static final String STRIPE_PROVIDER = "STRIPE";
    private final AuthService authService;
    private final OrderRepo orderRepo;
    private final StripeClient stripeClient;
    private final PaymentRepo paymentRepo;

    public PaymentServiceImpl(AuthService authService, OrderRepo orderRepo,
                              StripeClient stripeClient, PaymentRepo paymentRepo) {
        this.authService = authService;
        this.orderRepo = orderRepo;
        this.stripeClient = stripeClient;
        this.paymentRepo = paymentRepo;
    }

    @Override
    public CommonResponseDto<InitializePaymentResponseDto> initializePayment(String orderNumber) {
        log.info("Initializing payment for order: {}", orderNumber);

        if (orderNumber == null || orderNumber.isBlank()) {
            throw new ApplicationException("400", "Order number is required",
                                           HttpStatus.BAD_REQUEST);
        }

        User user = authService.getUser();

        /*
         * Locking prevents two simultaneous requests from creating
         * separate database payment records for the same order.
         */
        Order order = orderRepo
                .findByOrderNumberAndUserIdForUpdate(orderNumber, user.getId())
                .orElseThrow(() -> new ApplicationException("404", "Order not found for user",
                                                            HttpStatus.NOT_FOUND));

        validateOrderForPayment(order);

        try {
            /*
             * Reuse the existing PaymentIntent when the customer
             * clicks Pay more than once or refreshes the page.
             */
            Payment existingPayment = paymentRepo
                    .findByOrderIdAndPaymentProvider(order.getId(), STRIPE_PROVIDER)
                    .orElse(null);

            PaymentIntent paymentIntent;

            if (existingPayment != null) {
                paymentIntent = stripeClient
                        .v1()
                        .paymentIntents()
                        .retrieve(existingPayment.getPaymentId(), null, null);
            } else {
                paymentIntent = createStripePaymentIntent(order, user);

                Payment payment = Payment
                        .builder()
                        .order(order)
                        .paymentProvider(STRIPE_PROVIDER)
                        .paymentId(paymentIntent.getId())
                        .paymentStatus(PaymentStatus.PENDING)
                        .paymentMethod(PaymentMethod.CARD)
                        .paymentCurrency(order.getCurrency())
                        .paymentAmount(order.getTotalAmt())
                        .build();

                paymentRepo.save(payment);

                order.setPaymentReference(paymentIntent.getId());
            }

            InitializePaymentResponseDto initializePaymentResponseDto = InitializePaymentResponseDto
                    .builder()
                    .orderNumber(order.getOrderNumber())
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .amount(paymentIntent.getAmount())
                    .currency(paymentIntent.getCurrency())
                    .expiresAt(order.getExpiresAt())
                    .build();

            return CommonResponseDto
                    .<InitializePaymentResponseDto>builder()
                    .data(initializePaymentResponseDto)
                    .success(true)
                    .message("Payment initialized successfully")
                    .status(HttpStatus.OK.value())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe API error: {}", e.getMessage(), e);
            throw new ApplicationException("STRIPE_PAYMENT_ERROR",
                                           "Unable to initialize payment. Please try again.",
                                           HttpStatus.BAD_GATEWAY);
        }
    }

    // ========================================================================================
    // HELPER METHODS
    // ========================================================================================

    private PaymentIntent createStripePaymentIntent(Order order, User user) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams
                .builder()
                .setAmount(convertToMinorUnit(order.getTotalAmt()))
                .setCurrency(order
                                     .getCurrency()
                                     .toLowerCase(Locale.ROOT))
                .addPaymentMethodType(PaymentMethod.CARD
                                              .name()
                                              .toLowerCase(Locale.ROOT))
                .setReceiptEmail(user.getEmail())
                .setDescription("Payment for order: " + order.getOrderNumber())
                .putMetadata("order_number", order.getOrderNumber())
                .putMetadata("order_id", order
                        .getId()
                        .toString())
                .putMetadata("user_id", user
                        .getId()
                        .toString())
                .build();

        /*
         * Stripe returns the same PaymentIntent if the same request
         * is retried with this idempotency key.
         */
        RequestOptions requestOptions = RequestOptions
                .builder()
                .setIdempotencyKey("payment-intent-" + order.getOrderNumber())
                .setMaxNetworkRetries(2)
                .build();

        return stripeClient
                .v1()
                .paymentIntents()
                .create(params, requestOptions);
    }

    private long convertToMinorUnit(BigDecimal amount) {
        try {
            /*
             * AUD 132.50 becomes 13250 cents.
             */
            return amount
                    .movePointRight(2)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();
        } catch (ArithmeticException exception) {
            throw new ApplicationException("INVALID_PAYMENT_AMOUNT",
                                           "Order amount cannot be converted to AUD cents",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateOrderForPayment(Order order) {
        if (order.getStatus() == null || !order
                .getStatus()
                .equals(OrderStatus.PENDING_PAYMENT)) {
            throw new ApplicationException("409", "Order is not in a valid state for payment",
                                           HttpStatus.CONFLICT);
        }

        if (!order
                .getPaymentStatus()
                .equals(PaymentStatus.PENDING)) {
            throw new ApplicationException("409", "Payment has already been processed",
                                           HttpStatus.CONFLICT);
        }

        if (order.getExpiresAt() != null && !order
                .getExpiresAt()
                .isAfter(LocalDateTime.now())) {
            throw new ApplicationException("409", "Order payment session has expired",
                                           HttpStatus.CONFLICT);
        }

        if (order.getTotalAmt() == null || order
                                                   .getTotalAmt()
                                                   .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ApplicationException("409", "Order has an invalid payment amount",
                                           HttpStatus.CONFLICT);
        }
    }
}
