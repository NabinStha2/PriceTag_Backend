package com.example.pricetag.features.user.repository;

import com.example.pricetag.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(int id);

//  User findByAppUserRole(AppUserRole appUserRole);

}
