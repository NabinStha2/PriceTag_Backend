package com.example.pricetag.features.auth.service;

import com.example.pricetag.dto.EmailDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.entity.Otp;
import com.example.pricetag.features.auth.repository.OtpRepo;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import com.example.pricetag.utils.EmailUtils;
import com.example.pricetag.utils.OtpGeneratorUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OtpService {
    private final OtpGeneratorUtil otpGeneratorUtil;
    private final EmailUtils emailUtils;
    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OtpService(OtpGeneratorUtil otpGeneratorUtil, EmailUtils emailUtils, OtpRepo otpRepo,
                      UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.otpGeneratorUtil = otpGeneratorUtil;
        this.emailUtils = emailUtils;
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Integer generateAndSendOtp(String email) throws ApplicationException {
        Integer otpCode = otpGeneratorUtil.generateOTP(email);
        try {
            EmailDto emailDto = new EmailDto();
            emailDto.setRecipients(List.of(email));
            emailDto.setSubject("Otp Code");
            emailDto.setBody("Your otp code is: " + otpCode);
            emailUtils.sendOtpEmail(emailDto);
        } catch (MessagingException e) {
            throw new ApplicationException("500", "Unable to send otp please try again",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return otpCode;
    }

    public void saveOtp(User user, Integer otpCode) {
        otpRepo
                .findByUser(user)
                .ifPresent(otpRepo::delete);

        Otp newOtp = new Otp();
        newOtp.setOtpCode(otpCode);
        newOtp.setUser(user);
        otpRepo.save(newOtp);
    }

    public void validateOtp(User user, Integer otpCode, int validityMinutes)
            throws ApplicationException {
        Optional<Otp> existingOtp = otpRepo.findByUser(user);

        if (existingOtp.isEmpty()) {
            throw new ApplicationException("404", "Otp not found", HttpStatus.NOT_FOUND);
        }

        Otp otp = existingOtp.get();

        if (!Objects.equals(otp.getOtpCode(), otpCode)) {
            throw new ApplicationException("400", "Otp incorrect", HttpStatus.BAD_REQUEST);
        }

        if (Duration
                    .between(otp.getOtpGenerationTime(), LocalDateTime.now())
                    .getSeconds() > (validityMinutes * 60L)) {
            otpRepo.delete(otp);
            throw new ApplicationException("400", "Otp Expired", HttpStatus.BAD_REQUEST);
        }

        otpRepo.delete(otp);
    }

    public void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
