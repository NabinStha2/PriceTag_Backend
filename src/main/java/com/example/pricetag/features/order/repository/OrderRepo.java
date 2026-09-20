package com.example.pricetag.features.order.repository;

import com.example.pricetag.enums.OrderStatus;
import com.example.pricetag.features.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    //  @EntityGraph loads the order and its items together. Since this
    //  fetches only one order, loading the collection with an entity
    //  graph is appropriate.
    @EntityGraph(attributePaths = {"orderItems"})
    Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            join fetch o.user
            where o.orderNumber = :orderNumber
            and o.user.id = :userId
            """)
    Optional<Order> findByOrderNumberAndUserIdForUpdate(
            @Param("orderNumber")
            String orderNumber,
            @Param("userId")
            Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            join fetch o.user
            where o.id = :orderId
            """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId")
            Long orderId);

    @Query("""
            select o.id
            from Order o
            where o.status = OrderStatus.PENDING_PAYMENT
              and o.expiresAt <= :now
            order by o.id
            """)
    List<Long> findExpiredPendingPaymentOrderIds(
            @Param("now")
            LocalDateTime now, Pageable pageable);

    Optional<Order> findByUserIdAndCheckoutKey(Long userId, String checkoutKey);

    Page<Order> findAllByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

}
