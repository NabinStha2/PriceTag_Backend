package com.example.pricetag.features.auth.service;

import com.example.pricetag.enums.AppUserRole;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.dto.requestDto.RegisterRequestDto;
import com.example.pricetag.features.auth.entity.Role;
import com.example.pricetag.features.auth.repository.AuthRepository;
import com.example.pricetag.features.auth.repository.RoleRepository;
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
    private final RoleRepository roleRepository;

    @Autowired
    public UserRegistrationService(AuthRepository authRepository, PasswordEncoder passwordEncoder,
                                   OtpService otpService, RoleRepository roleRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.roleRepository = roleRepository;
    }

    public User registerNewUser(RegisterRequestDto registerRequestDto) throws ApplicationException {
        // Don't log registerRequestDto.toString() here - if that DTO has a
        // "password" field without @ToString.Exclude, this would write the
        // user's plaintext password into your logs. Log specific safe
        // fields instead, e.g.:
        ColorLogger.logInfo("Registering new user with email: " + registerRequestDto.getEmail());

        User existingUser = authRepository
                .findByEmail(registerRequestDto.getEmail())
                .orElse(null);

        if (existingUser != null && existingUser.isVerified()) {
            throw new ApplicationException("400", "Email already exists", HttpStatus.BAD_REQUEST);
        }

        if (existingUser != null) {
            authRepository.delete(existingUser);
        }

        // Look up the existing ROLE_USER row instead of building a new,
        // unmanaged Role every time someone registers. Building a new one
        // (as before) either fails to save (no cascade configured on
        // User.roles) or creates a fresh duplicate role row per user.
        Role userRole = roleRepository
                .findByName(AppUserRole.ROLE_USER.name())
                .orElseThrow(() -> new ApplicationException("500", "Default role not configured",
                                                            HttpStatus.INTERNAL_SERVER_ERROR));

        User newUser = new User();
        newUser.setEmail(registerRequestDto.getEmail());
        newUser.setName(registerRequestDto.getFullName());
        newUser.setPhoneNumber(registerRequestDto.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        newUser.setRoles(Set.of(userRole));
        newUser.setVerified(false);

        User savedUser = authRepository.save(newUser);
        ColorLogger.logInfo("Created new user with id: " + savedUser.getId());

        return savedUser;
    }

    public void initiateRegistration(RegisterRequestDto registerRequestDto)
            throws ApplicationException {
        User newUser = registerNewUser(registerRequestDto);
        Integer otpCode = otpService.generateAndSendOtp(registerRequestDto.getEmail());
        otpService.saveOtp(newUser, otpCode);
    }
}