package com.example.pricetag.features.auth.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequestDto {
    private String email;
    private int otpCode;
    private String newPassword;
}
