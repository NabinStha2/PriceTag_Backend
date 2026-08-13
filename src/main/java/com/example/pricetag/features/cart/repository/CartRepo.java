package com.example.pricetag.features.cart.repository;


import com.example.pricetag.features.cart.entity.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT distinct c FROM Cart c " + "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.variant v " + "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdForCheckout(
            @Param("userId")
            Long userId);
}
