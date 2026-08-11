package com.example.pricetag.features.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressSnapshot {

    @Column(name = "shipping_source_address_id")
    private Long sourceAddressId;

    @Column(name = "shipping_recipient_name",
            nullable = false,
            length = 100)
    private String recipientName;

    @Column(name = "shipping_phone",
            nullable = false,
            length = 30)
    private String phone;

    @Column(name = "shipping_unit_number",
            length = 30)
    private String unitNumber;

    @Column(name = "shipping_street_number",
            length = 30)
    private String streetNumber;

    @Column(name = "shipping_street",
            nullable = false,
            length = 150)
    private String street;

    @Column(name = "shipping_city",
            nullable = false,
            length = 100)
    private String city;

    @Column(name = "shipping_state",
            nullable = false,
            length = 100)
    private String state;

    @Column(name = "shipping_country",
            nullable = false,
            length = 100)
    private String country;

    @Column(name = "shipping_postal_code",
            nullable = false,
            length = 20)
    private String postalCode;
}
