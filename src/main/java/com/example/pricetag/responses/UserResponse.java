package com.example.pricetag.responses;

import com.example.pricetag.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
    private String email;
    private String phoneNumber;
    private Set<String> appUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
