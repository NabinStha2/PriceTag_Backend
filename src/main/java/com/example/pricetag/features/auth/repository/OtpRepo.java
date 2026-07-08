package com.example.pricetag.features.auth.repository;

import com.example.pricetag.features.auth.entity.Otp;
import com.example.pricetag.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepo extends JpaRepository<Otp, Long> {

    Optional<Otp> findByUser(User user);

}
