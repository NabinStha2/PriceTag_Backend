package com.example.pricetag.features.auth.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private boolean success;
    private String message;
    private AuthResponseDataDto data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponseDataDto {
        private String accessToken;
        private String refreshToken;
    }
}
