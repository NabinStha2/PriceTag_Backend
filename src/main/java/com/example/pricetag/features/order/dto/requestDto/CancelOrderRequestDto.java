package com.example.pricetag.features.order.dto.requestDto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequestDto {

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500,
            message = "Cancellation reason must be at most 500 characters")
    private String reason;
}