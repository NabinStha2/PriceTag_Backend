package com.example.pricetag.features.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequestDto {

    private Long id;

    @Size(max = 50,
            message = "Unit number cannot exceed 50 characters")
    private String unitNumber;

    @Size(max = 50,
            message = "Street number cannot exceed 50 characters")
    private String streetNumber;

    @NotBlank(message = "Street is required")
    @Size(max = 150,
            message = "Street cannot exceed 150 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100,
            message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100,
            message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100,
            message = "Country cannot exceed 100 characters")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20,
            message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Size(max = 50,
            message = "Label cannot exceed 50 characters")
    private String label; // e.g., "Home", "Office"

    private Boolean isDefault;
}
