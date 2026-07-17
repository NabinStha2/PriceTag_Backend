package com.example.pricetag.features.cart.repository;

import com.example.pricetag.features.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    List<CartItem> findAllByCartId(Long cartId);


}
