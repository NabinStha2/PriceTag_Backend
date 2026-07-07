package com.example.pricetag.services;

import com.example.pricetag.config.AuthDetails;
import com.example.pricetag.dto.*;
import com.example.pricetag.entity.User;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.repository.AuthRepository;
import com.example.pricetag.repository.UserRepo;
import com.example.pricetag.responses.AuthResponseDto;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    public AuthService(AuthRepository authRepository, UserRepo userRepo,
                       UserRegistrationService userRegistrationService, OtpService otpService,
                       TokenService tokenService) {
        this.authRepository = authRepository;
        this.userRepo = userRepo;
        this.userRegistrationService = userRegistrationService;
        this.otpService = otpService;
        this.tokenService = tokenService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ColorLogger.logInfo("I am inside AuthService loadUserByUsername");
        Optional<User> user = authRepository.findByEmail(username);
        return user
                .map(AuthDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + username));
    }

    public CommonResponseDto register(RegisterUserDto registerUserDto) throws ApplicationException {
        ColorLogger.logInfo("I am inside AuthService register" + registerUserDto.toString());

        userRegistrationService.initiateRegistration(registerUserDto);

        return CommonResponseDto
                .builder()
                .success(true)
                .message("Otp code has been sent successfully")
                .build();
    }

    public AuthResponseDto verifyOtp(OtpDto otpDto) throws ApplicationException {
        ColorLogger.logInfo("I am inside AuthService verifyOtp");
        User existingUser = this.authRepository
                .findByEmail(otpDto.getEmail())
                .orElse(null);

        if (existingUser == null || existingUser.isVerified()) {
            throw new ApplicationException("400", "Invalid verification request",
                                           HttpStatus.BAD_REQUEST);
        }

        otpService.validateOtp(existingUser, otpDto.getOtpCode(), 5);
        existingUser.setVerified(true);
        userRepo.save(existingUser);

        return tokenService.createAuthResponse(otpDto.getEmail(), "Email verified successfully");
    }

    public AuthResponseDto getNewTokenByRefreshToken(RefreshTokenDto refreshTokenDto)
            throws ApplicationException {
        return tokenService.refreshToken(refreshTokenDto);
    }

    public AuthResponseDto login(AuthDto authDto) throws ApplicationException {
        User user = this.userRepo
                .findByEmail(authDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        ColorLogger.logError(user.toString());

        if (!user.isVerified()) {
            throw new ApplicationException("400",
                                           "Account has been not verified. Please verify your account",
                                           HttpStatus.BAD_REQUEST);
        }

        tokenService.cleanupExistingRefreshTokens(user);
        return tokenService.createAuthResponse(authDto.getEmail(), "Login successful");
    }

    public CommonResponseDto forgotPassword(ForgotPasswordDto forgotPasswordDto)
            throws ApplicationException {
        ColorLogger.logInfo("I am inside AuthService forgotPassword");

        User existingUser = authRepository
                .findByEmail(forgotPasswordDto.getEmail())
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

    public CommonResponseDto verifyForgotPasswordOtp(OtpDto otpDto) throws ApplicationException {
        ColorLogger.logInfo("I am inside AuthService verifyForgotPasswordOtp");
        User existingUser = this.authRepository
                .findByEmail(otpDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        if (!existingUser.isVerified()) {
            throw new ApplicationException("400", "Account not verified", HttpStatus.BAD_REQUEST);
        }

        otpService.validateOtp(existingUser, otpDto.getOtpCode(), 1);
        otpService.resetPassword(existingUser, otpDto.getNewPassword());

        return CommonResponseDto
                .builder()
                .message("Password reset successfully")
                .success(true)
                .build();
    }

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