package com.example.pricetag.features.address.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDto {

    private Long id;

    private String unitNumber;

    private String streetNumber;

    private String street;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private String label;

    private Boolean isDefault;
}