package com.example.pricetag.features.auth.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.dto.requestDto.*;
import com.example.pricetag.features.auth.dto.responseDto.AuthResponseDto;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Spring Security tutorials !!";
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponseDto> register(
            @RequestBody
            RegisterRequestDto registerRequestDto) throws ApplicationException {
        return new ResponseEntity<>(authService.register(registerRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @RequestBody
            OtpRequestDto otpRequestDto) throws ApplicationException {
        return new ResponseEntity<>(authService.verifyOtp(otpRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody
            AuthRequestDto authDto) {
        return ResponseEntity.ok(authService.login(authDto));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @RequestBody
            RefreshTokenRequestDto refreshTokenRequestDto) throws ApplicationException {
        // NOTE: previously this logged the raw refresh token value.
        // Refresh tokens are long-lived credentials - logging them puts a
        // usable "log in as this user" secret into your log files, which
        // are usually far less protected than your database. Don't log
        // token/password/secret values, ever, even at debug level.
        ColorLogger.logInfo("Refresh token request received");
        return ResponseEntity.ok(authService.getNewTokenByRefreshToken(refreshTokenRequestDto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponseDto> forgotPassword(
            @RequestBody
            ForgotPasswordRequestDto forgotPasswordRequestDto) throws ApplicationException {
        return ResponseEntity.ok(authService.forgotPassword(forgotPasswordRequestDto));
    }

    @PatchMapping("/verify-forgot-password-otp")
    public ResponseEntity<CommonResponseDto> verifyForgotPasswordOtp(
            @RequestBody
            OtpRequestDto otpRequestDto) throws ApplicationException {
        return ResponseEntity.ok(authService.verifyForgotPasswordOtp(otpRequestDto));
    }
}