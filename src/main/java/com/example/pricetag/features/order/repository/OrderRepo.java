package com.example.pricetag.features.order.repository;

import com.example.pricetag.features.address.entity.Address;
import com.example.pricetag.features.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);
}
