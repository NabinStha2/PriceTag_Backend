package com.example.pricetag.features.auth.service;

import com.example.pricetag.config.AuthDetails;
import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.dto.requestDto.*;
import com.example.pricetag.features.auth.dto.responseDto.AuthResponseDto;
import com.example.pricetag.features.auth.repository.AuthRepository;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {
    private final AuthRepository authRepository;
    private final UserRepo userRepo;
    private final UserRegistrationService userRegistrationService;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(AuthRepository authRepository, UserRepo userRepo,
                       UserRegistrationService userRegistrationService, OtpService otpService,
                       TokenService tokenService,
                       @Lazy
                       AuthenticationManager authenticationManager) {
        this.authRepository = authRepository;
        this.userRepo = userRepo;
        this.userRegistrationService = userRegistrationService;
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Called internally by Spring Security whenever it needs to look up a
     * user by email - both during login (via AuthenticationManager) and
     * when checking a JWT on every request (via JwtFilter).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = authRepository.findByEmail(username);
        return user
                .map(AuthDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public CommonResponseDto register(RegisterRequestDto registerRequestDto)
            throws ApplicationException {
        userRegistrationService.initiateRegistration(registerRequestDto);
        return CommonResponseDto
                .builder()
                .success(true)
                .message("Otp code has been sent successfully")
                .build();
    }

    public AuthResponseDto verifyOtp(OtpRequestDto otpRequestDto) throws ApplicationException {
        User existingUser = this.authRepository
                .findByEmail(otpRequestDto.getEmail())
                .orElse(null);

        if (existingUser == null || existingUser.isVerified()) {
            throw new ApplicationException("400", "Invalid verification request",
                                           HttpStatus.BAD_REQUEST);
        }

        otpService.validateOtp(existingUser, otpRequestDto.getOtpCode(), 5);
        existingUser.setVerified(true);
        userRepo.save(existingUser);

        return tokenService.createAuthResponse(otpRequestDto.getEmail(),
                                               "Email verified successfully");
    }

    public AuthResponseDto getNewTokenByRefreshToken(RefreshTokenRequestDto refreshTokenRequestDto)
            throws ApplicationException {
        return tokenService.refreshToken(refreshTokenRequestDto);
    }

    /**
     * Real password check happens here, via AuthenticationManager ->
     * DaoAuthenticationProvider -> BCryptPasswordEncoder (see
     * SecurityConfig). If the email doesn't exist or the password is
     * wrong, authenticate() throws - we catch it below and turn it into
     * one generic error message.
     */
    public AuthResponseDto login(AuthRequestDto authDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(),
                                                            authDto.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Deliberately vague: don't tell the caller whether the email
            // existed or the password was wrong. Either detail helps an
            // attacker enumerate valid accounts.
            throw new ApplicationException("401", "Invalid email or password",
                                           HttpStatus.UNAUTHORIZED);
        }

        User user = this.userRepo
                .findByEmail(authDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        if (!user.isVerified()) {
            throw new ApplicationException("400",
                                           "Account has been not verified. Please verify your account",
                                           HttpStatus.BAD_REQUEST);
        }

        tokenService.cleanupExistingRefreshTokens(user);
        return tokenService.createAuthResponse(authDto.getEmail(), "Login successful");
    }

    public CommonResponseDto forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto)
            throws ApplicationException {
        User existingUser = authRepository
                .findByEmail(forgotPasswordRequestDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        Integer otpCode = otpService.generateAndSendOtp(existingUser.getEmail());
        otpService.saveOtp(existingUser, otpCode);

        return CommonResponseDto
                .builder()
                .message("Otp sent successfully")
                .success(true)
                .build();
    }

    public CommonResponseDto verifyForgotPasswordOtp(OtpRequestDto otpRequestDto)
            throws ApplicationException {
        User existingUser = this.authRepository
                .findByEmail(otpRequestDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        if (!existingUser.isVerified()) {
            throw new ApplicationException("400", "Account not verified", HttpStatus.BAD_REQUEST);
        }

        otpService.validateOtp(existingUser, otpRequestDto.getOtpCode(), 1);
        otpService.resetPassword(existingUser, otpRequestDto.getNewPassword());

        return CommonResponseDto
                .builder()
                .message("Password reset successfully")
                .success(true)
                .build();
    }

    /**
     * Reads the currently logged-in user back out of the security context.
     */
    public User getUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null ||
            !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApplicationException("401", "User not authenticated",
                                           HttpStatus.UNAUTHORIZED);
        }
        return userRepo
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApplicationException("404", "User not found",
                                                            HttpStatus.NOT_FOUND));
    }

    public Long getCurrentUserId() {
        AuthDetails authDetails = (AuthDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return authDetails.getId();
    }
}