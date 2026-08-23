package com.example.pricetag.features.payment.repository;

import com.example.pricetag.features.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndPaymentProvider(Long orderId, String paymentProvider);


    /*
    Why lock the payment?

    Stripe can send the same event more than once. Locking ensures
    that two webhook requests cannot process the same payment
    concurrently.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentId = :paymentId")
    Optional<Payment> findByPaymentIdForUpdate(
            @Param("paymentId")
            String paymentId);

    @Query("select p.order.id from Payment p where p.paymentId = :paymentId")
    Optional<Long> findOrderIdByPaymentId(@Param("paymentId") String paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Payment p
            where p.order.id = :orderId
              and p.paymentProvider = :paymentProvider
            """)
    Optional<Payment> findByOrderIdAndPaymentProviderForUpdate(
            @Param("orderId") Long orderId,
            @Param("paymentProvider") String paymentProvider);

}
