package com.example.pricetag.features.order.service;

import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.enums.PaymentMethod;
import com.example.pricetag.enums.PaymentStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.Variant.entity.Variants;
import com.example.pricetag.features.Variant.repository.VariantRepo;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.order.entity.Order;
import com.example.pricetag.features.order.entity.OrderItem;
import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.payment.entity.Payment;
import com.example.pricetag.features.payment.repository.PaymentRepo;
import com.example.pricetag.features.payment.service.StripePaymentIntentService;
import com.example.pricetag.features.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private PaymentRepo paymentRepo;
    @Mock
    private VariantRepo variantRepo;
    @Mock
    private CartRepo cartRepo;
    @Mock
    private StripePaymentIntentService stripePaymentIntentService;

    private OrderLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new OrderLifecycleService(orderRepo, paymentRepo, variantRepo, cartRepo,
                                            stripePaymentIntentService);
    }

    @Test
    void cancelPendingOrderCancelsPaymentAndReleasesReservation() {
        TestData data = pendingOrder(LocalDateTime.now().plusMinutes(10));
        Payment payment = pendingPayment(data.order());

        when(orderRepo.findByOrderNumberAndUserIdForUpdate("PT-1", 7L))
                .thenReturn(Optional.of(data.order()));
        when(paymentRepo.findByOrderIdAndPaymentProviderForUpdate(10L, "STRIPE"))
                .thenReturn(Optional.of(payment));
        when(variantRepo.findAllByIdForUpdate(List.of(20L)))
                .thenReturn(List.of(data.variant()));
        when(cartRepo.findByUserIdForCheckout(7L)).thenReturn(Optional.of(data.cart()));

        Order result = service.cancelPendingOrder("PT-1", 7L, "Changed my mind");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(PaymentStatus.CANCELLED, result.getPaymentStatus());
        assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());
        assertEquals(7, data.variant().getStockQuantity());
        assertTrue(result.isStockRestored());
        assertEquals(CartStatus.ACTIVE, data.cart().getStatus());
        assertEquals(1, result.getStatusHistory().size());
        verify(stripePaymentIntentService).cancel("pi_123");
    }

    @Test
    void expirePendingOrderWithoutPaymentStillRestoresStock() {
        TestData data = pendingOrder(LocalDateTime.now().minusMinutes(1));

        when(orderRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(data.order()));
        when(paymentRepo.findByOrderIdAndPaymentProviderForUpdate(10L, "STRIPE"))
                .thenReturn(Optional.empty());
        when(variantRepo.findAllByIdForUpdate(List.of(20L)))
                .thenReturn(List.of(data.variant()));
        when(cartRepo.findByUserIdForCheckout(7L)).thenReturn(Optional.of(data.cart()));

        assertTrue(service.expirePendingOrder(10L));
        assertEquals(OrderStatus.EXPIRED, data.order().getStatus());
        assertEquals(PaymentStatus.EXPIRED, data.order().getPaymentStatus());
        assertEquals(7, data.variant().getStockQuantity());
        verifyNoInteractions(stripePaymentIntentService);
    }

    @Test
    void paidOrderCannotBeCancelled() {
        TestData data = pendingOrder(LocalDateTime.now().plusMinutes(10));
        data.order().setPaymentStatus(PaymentStatus.SUCCEEDED);

        when(orderRepo.findByOrderNumberAndUserIdForUpdate("PT-1", 7L))
                .thenReturn(Optional.of(data.order()));
        when(paymentRepo.findByOrderIdAndPaymentProviderForUpdate(10L, "STRIPE"))
                .thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.cancelPendingOrder("PT-1", 7L, "Changed my mind"));

        assertEquals("PAYMENT_ALREADY_SUCCEEDED", exception.getErrorCode());
        assertEquals(5, data.variant().getStockQuantity());
        verifyNoInteractions(variantRepo, cartRepo, stripePaymentIntentService);
    }

    @Test
    void expiryIsRecheckedAfterTheOrderLockIsAcquired() {
        TestData data = pendingOrder(LocalDateTime.now().plusMinutes(1));
        when(orderRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(data.order()));

        assertFalse(service.expirePendingOrder(10L));
        verifyNoInteractions(paymentRepo, variantRepo, cartRepo, stripePaymentIntentService);
    }

    private TestData pendingOrder(LocalDateTime expiresAt) {
        User user = User.builder().id(7L).email("buyer@example.com").build();
        Variants variant = Variants.builder().id(20L).sku("SKU-1").stockQuantity(5).build();
        Cart cart = Cart.builder().user(user).status(CartStatus.CHECKOUT).build();
        Order order = Order
                .builder()
                .id(10L)
                .user(user)
                .orderNumber("PT-1")
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
        order.addOrderItem(OrderItem
                                   .builder()
                                   .variants(variant)
                                   .quantity(2)
                                   .sku("SKU-1")
                                   .unitPrice(BigDecimal.TEN)
                                   .totalPrice(BigDecimal.valueOf(20))
                                   .productName("Product")
                                   .build());
        return new TestData(order, variant, cart);
    }

    private Payment pendingPayment(Order order) {
        return Payment
                .builder()
                .order(order)
                .paymentProvider("STRIPE")
                .paymentId("pi_123")
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.CARD)
                .paymentCurrency("AUD")
                .paymentAmount(BigDecimal.TEN)
                .build();
    }

    private record TestData(Order order, Variants variant, Cart cart) {
    }
}
