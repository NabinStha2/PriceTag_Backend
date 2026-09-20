package com.example.pricetag.features.cart.service;

import com.example.pricetag.enums.CartStatus;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.cart.entity.Cart;
import com.example.pricetag.features.cart.repository.CartRepo;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class CartAccessService {

    private final UserRepo userRepo;
    private final CartRepo cartRepo;
    private final EntityManager entityManager;

    public CartAccessService(UserRepo userRepo, CartRepo cartRepo, EntityManager entityManager) {

        this.userRepo = userRepo;
        this.cartRepo = cartRepo;
        this.entityManager = entityManager;
    }

    public User lockUser(User authenticatedUser) {
        return userRepo
                .findByIdForUpdate(authenticatedUser.getId())
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found",
                                                            HttpStatus.NOT_FOUND));
    }

    public Cart getOrCreate(User authenticatedUser) {
        User user = lockUser(authenticatedUser);

        return findAndRefreshCart(user.getId()).orElseGet(() -> cartRepo.saveAndFlush(Cart
                                                                                              .builder()
                                                                                              .user(user)
                                                                                              .status(CartStatus.ACTIVE)
                                                                                              .build()));
    }

    public Cart requireExisting(User authenticatedUser) {
        User user = lockUser(authenticatedUser);

        return findAndRefreshCart(user.getId()).orElseThrow(
                () -> new ApplicationException("CART_NOT_FOUND", "Cart not found for user",
                                               HttpStatus.NOT_FOUND));
    }

    private Optional<Cart> findAndRefreshCart(Long userId) {
        Optional<Cart> result = cartRepo.findByUserIdForUpdate(userId);

        result.ifPresent(cart -> entityManager.refresh(cart, LockModeType.PESSIMISTIC_WRITE));

        return result;
    }
}