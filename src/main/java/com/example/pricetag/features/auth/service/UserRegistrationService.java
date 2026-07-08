package com.example.pricetag.features.auth.service;

import com.example.pricetag.enums.AppUserRole;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.dto.requestDto.RegisterRequestDto;
import com.example.pricetag.features.auth.entity.Role;
import com.example.pricetag.features.auth.repository.AuthRepository;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserRegistrationService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Autowired
    public UserRegistrationService(AuthRepository authRepository, PasswordEncoder passwordEncoder,
                                   OtpService otpService) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public User registerNewUser(RegisterRequestDto registerRequestDto) throws ApplicationException {
        ColorLogger.logInfo("Registering new user: " + registerRequestDto.toString());

        User existingUser = authRepository
                .findByEmail(registerRequestDto.getEmail())
                .orElseThrow(() -> new ApplicationException("404", "Email not found",
                                                            HttpStatus.NOT_FOUND));

        if (existingUser.isVerified()) {
            throw new ApplicationException("400", "Email already exists", HttpStatus.BAD_REQUEST);
        }

        authRepository.delete(existingUser);

        User newUser = new User();
        newUser.setEmail(registerRequestDto.getEmail());
        newUser.setName(registerRequestDto.getFullName());
        newUser.setPhoneNumber(registerRequestDto.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        newUser.setRoles(Set.of(Role
                                        .builder()
                                        .name(AppUserRole.ROLE_USER.name())
                                        .build()));
        newUser.setVerified(false);

        ColorLogger.logInfo("Created new user: " + newUser);

        return authRepository.save(newUser);
    }

    public void initiateRegistration(RegisterRequestDto registerRequestDto)
            throws ApplicationException {
        User newUser = registerNewUser(registerRequestDto);
        Integer otpCode = otpService.generateAndSendOtp(registerRequestDto.getEmail());
        otpService.saveOtp(newUser, otpCode);
    }
}
