package com.example.pricetag.features.payment.repository;

import com.example.pricetag.features.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndPaymentProvider(Long orderId, String paymentProvider);
}
