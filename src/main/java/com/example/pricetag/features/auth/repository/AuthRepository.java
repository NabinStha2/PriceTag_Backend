package com.example.pricetag.features.auth.repository;

import com.example.pricetag.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String userName);


}