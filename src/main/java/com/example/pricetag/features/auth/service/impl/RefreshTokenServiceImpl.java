package com.example.pricetag.features.auth.service.impl;

import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.entity.RefreshToken;
import com.example.pricetag.features.auth.repository.AuthRepository;
import com.example.pricetag.features.auth.repository.RefreshTokenRepo;
import com.example.pricetag.features.auth.service.RefreshTokenService;
import com.example.pricetag.features.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final AuthRepository authRepository;
    private final RefreshTokenRepo refreshTokenRepo;

    public RefreshTokenServiceImpl(AuthRepository authRepository,
                                   RefreshTokenRepo refreshTokenRepo) {
        this.authRepository = authRepository;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Override
    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = RefreshToken
                .builder()
                .user(this.authRepository
                              .findByEmail(email)
                              .orElseThrow(() -> new ApplicationException("404", "User not found",
                                                                          HttpStatus.NOT_FOUND)))
                .token(UUID
                               .randomUUID()
                               .toString())
                .expiryDate(Instant
                                    .now()
                                    .plusMillis(600000)) // 10 minute
                .build();

        return refreshTokenRepo.save(refreshToken);

    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    @Override
    public RefreshToken verifyRefreshTokenExpirationDate(RefreshToken refreshToken) {
        if (refreshToken
                    .getExpiryDate()
                    .compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete(refreshToken);
            throw new ApplicationException("400",
                                           "Refresh Token already expired. Please Sign In to continue.",
                                           HttpStatus.BAD_REQUEST);
        }

        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> getRefreshTokenByUser(User user) {
        return refreshTokenRepo.findByUser(user);
    }

}
