package com.example.pricetag.features.cart.repository;


import com.example.pricetag.features.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByIdAndStatus(Long id, Cart.CartStatus status);

    Optional<Cart> findByUserIdAndStatus(Long userId, Cart.CartStatus status);
}
