package com.example.pricetag.features.order.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequestDto {
    @NotNull(message = "Address id is required")
    private Long addressId;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100,
            message = "Recipient name must be at most 100 characters")
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 30,
            message = "Phone number must be at most 30 characters")
    private String phoneNumber;
}
