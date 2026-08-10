package com.example.pricetag.features.address.entity;

import com.example.pricetag.entity.BaseEntity;
import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "addresses",
        indexes = {@Index(columnList = "user_id",
                name = "idx_address_user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @Column(name = "unit_number")
    private String unitNumber;

    @Column(name = "street_number")
    private String streetNumber;

    @Column(name = "street",
            nullable = false,
            length = 150)
    private String street;

    @Column(name = "city",
            nullable = false,
            length = 100)
    private String city;

    @Column(name = "state",
            nullable = false,
            length = 100)
    private String state;

    @Column(name = "country",
            nullable = false,
            length = 100)
    private String country;

    @Column(name = "postal_code",
            nullable = false,
            length = 20)
    private String postalCode;

    @Column(name = "label",
            length = 50)
    private String label; // e.g., "Home", "Office"

    @Column(name = "is_default",
            nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}