package com.example.pricetag.features.order.dto.responseDto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressResponseDto {
    private Long sourceAddressId;
    private String recipientName;
    private String phone;

    private String unitNumber;
    private String streetNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
