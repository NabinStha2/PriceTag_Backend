package com.example.pricetag.features.cart.repository;

import com.example.pricetag.features.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {


}
