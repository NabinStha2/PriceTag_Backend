package com.example.pricetag.features.cart.repository;


import com.example.pricetag.features.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);
}
