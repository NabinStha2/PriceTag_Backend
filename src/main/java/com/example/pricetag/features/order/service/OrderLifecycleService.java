package com.example.pricetag.features.order.service;

import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.entity.OrderStatusHistory;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.payment.entity.Payment;
import com.example.pricetag.features.payment.repository.PaymentRepo;
import com.example.pricetag.features.payment.service.StripePaymentIntentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderLifecycleService {

    private static final String STRIPE_PROVIDER = "STRIPE";
    private static final String EXPIRY_REASON = "Payment window expired";

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final VariantRepo variantRepo;
    private final StripePaymentIntentService stripePaymentIntentService;

    public OrderLifecycleService(OrderRepo orderRepo, PaymentRepo paymentRepo,
                                 VariantRepo variantRepo, CartRepo cartRepo,
                                 StripePaymentIntentService stripePaymentIntentService) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.variantRepo = variantRepo;
        this.stripePaymentIntentService = stripePaymentIntentService;
    }

    @Transactional
    public Order cancelPendingOrder(String orderNumber, Long userId, String reason) {
        Order order = orderRepo
                .findByOrderNumberAndUserIdForUpdate(orderNumber, userId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND",
                                                            "Order not found for user",
                                                            HttpStatus.NOT_FOUND));

        requirePendingPayment(order);
        transition(order, OrderStatus.CANCELLED, PaymentStatus.CANCELLED, reason, order
                .getUser()
                .getId(), order
                           .getUser()
                           .getEmail());
        return order;
    }

    @Transactional
    public boolean expirePendingOrder(Long orderId) {
        Order order = orderRepo
                .findByIdForUpdate(orderId)
                .orElse(null);

        if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT ||
            order.getExpiresAt() == null || order
                    .getExpiresAt()
                    .isAfter(LocalDateTime.now())) {
            return false;
        }

        transition(order, OrderStatus.EXPIRED, PaymentStatus.EXPIRED, EXPIRY_REASON, null, null);
        return true;
    }

    private void transition(Order order, OrderStatus orderStatus, PaymentStatus paymentStatus,
                            String reason, Long actorUserId, String actorEmail) {
        Payment payment = paymentRepo
                .findByOrderIdAndPaymentProviderForUpdate(order.getId(), STRIPE_PROVIDER)
                .orElse(null);

        if (order.getPaymentStatus() == PaymentStatus.SUCCEEDED ||
            payment != null && payment.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
            throw new ApplicationException("PAYMENT_ALREADY_SUCCEEDED",
                                           "A paid order cannot be cancelled or expired",
                                           HttpStatus.CONFLICT);
        }

        if (payment != null) {
            stripePaymentIntentService.cancel(payment.getPaymentId());
            payment.setPaymentStatus(paymentStatus);
        }

        restoreStock(order);

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(orderStatus);
        order.setPaymentStatus(paymentStatus);
        order.setCancellationReason(reason);
        order.addStatusHistory(OrderStatusHistory
                                       .builder()
                                       .previousStatus(previousStatus)
                                       .newStatus(orderStatus)
                                       .note(reason)
                                       .actorUserId(actorUserId)
                                       .actorEmail(actorEmail)
                                       .build());
    }

    private void requirePendingPayment(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApplicationException("ORDER_NOT_PENDING_PAYMENT",
                                           "Only pending-payment orders can be cancelled",
                                           HttpStatus.CONFLICT);
        }
    }

    private void restoreStock(Order order) {
        if (order.isStockRestored()) {
            return;
        }

        List<Long> variantIds = order
                .getOrderItems()
                .stream()
                .map(item -> item
                        .getVariants()
                        .getId())
                .distinct()
                .sorted()
                .toList();

        if (variantIds.isEmpty()) {
            order.setStockRestored(true);
            return;
        }

        Map<Long, Variants> variantsById = variantRepo
                .findAllByIdForUpdate(variantIds)
                .stream()
                .collect(Collectors.toMap(Variants::getId, variant -> variant));

        order
                .getOrderItems()
                .forEach(item -> {
                    Variants variant = variantsById.get(item
                                                                .getVariants()
                                                                .getId());
                    if (variant == null) {
                        throw new ApplicationException("STOCK_RESTORE_FAILED",
                                                       "Unable to restore stock for SKU " +
                                                       item.getSku(), HttpStatus.CONFLICT);
                    }
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                });

        order.setStockRestored(true);
    }
}
