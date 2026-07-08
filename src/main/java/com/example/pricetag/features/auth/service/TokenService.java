package com.example.pricetag.features.auth.service;

import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.dto.requestDto.RefreshTokenRequestDto;
import com.example.pricetag.features.auth.dto.responseDto.AuthResponseDto;
import com.example.pricetag.features.auth.entity.RefreshToken;
import com.example.pricetag.features.auth.repository.RefreshTokenRepo;
import com.example.pricetag.features.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Handles issuing/refreshing the pair of tokens returned after login:
 * - accessToken:  short-lived JWT, sent on every API request
 * - refreshToken: longer-lived, used only to obtain a new accessToken
 * without asking the user to log in again
 * No security bugs found here - included for completeness / consistency
 * with the rest of the auth stack.
 */
@Service
public class TokenService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Autowired
    public TokenService(JwtService jwtService, RefreshTokenService refreshTokenService,
                        RefreshTokenRepo refreshTokenRepo) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    public AuthResponseDto createAuthResponse(String email, String message) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);
        String jwtToken = jwtService.generateToken(email);

        return AuthResponseDto
                .builder()
                .success(true)
                .message(message)
                .data(AuthResponseDto.AuthResponseDataDto
                              .builder()
                              .accessToken(jwtToken)
                              .refreshToken(refreshToken.getToken())
                              .build())
                .build();
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto)
            throws ApplicationException {
        return refreshTokenService
                .findByToken(refreshTokenRequestDto.getToken())
                .map(refreshTokenService::verifyRefreshTokenExpirationDate)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user.getEmail());
                    return AuthResponseDto
                            .builder()
                            .success(true)
                            .message("Token refreshed successfully")
                            .data(AuthResponseDto.AuthResponseDataDto
                                          .builder()
                                          .accessToken(accessToken)
                                          .build())
                            .build();
                })
                .orElseThrow(() -> new ApplicationException(null, "Refresh Token not found",
                                                            HttpStatus.BAD_REQUEST));
    }

    public void cleanupExistingRefreshTokens(User user) {
        RefreshToken existingRefreshToken = refreshTokenService
                .getRefreshTokenByUser(user)
                .orElseThrow(() -> new ApplicationException("", "", HttpStatus.UNAUTHORIZED));
        refreshTokenRepo.delete(existingRefreshToken);
    }
}