package com.example.pricetag.features.auth.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    private String email;
    private String password;

    @Override
    public String toString() {
        return "LoginUserDto{" +
               "email='" + email + '\'' +
               ", password='" + password + '\'' +
               '}';
    }
}