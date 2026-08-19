package com.example.pricetag.features.order.repository;

import com.example.pricetag.features.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
