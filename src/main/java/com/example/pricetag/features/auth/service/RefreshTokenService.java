package com.example.pricetag.features.auth.service;

import com.example.pricetag.features.auth.entity.RefreshToken;
import com.example.pricetag.features.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RefreshTokenService {

    RefreshToken createRefreshToken(String email);

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> getRefreshTokenByUser(User user);

    RefreshToken verifyRefreshTokenExpirationDate(RefreshToken refreshToken);

}
