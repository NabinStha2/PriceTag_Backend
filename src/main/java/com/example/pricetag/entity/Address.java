package com.example.pricetag.entity;

import com.example.pricetag.features.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "addresses",
        indexes = {@Index(columnList = "user_id",
                name = "idx_address_user_id"
        )}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            nullable = false
    )
    @JsonIgnore // prevent circular reference in JSON
    private User user;

    @Column(name = "street",
            nullable = false,
            length = 150
    )
    private String street;

    @Column(name = "city",
            nullable = false,
            length = 100
    )
    private String city;

    @Column(name = "state",
            nullable = false,
            length = 100
    )
    private String state;

    @Column(name = "country",
            nullable = false,
            length = 100
    )
    private String country;

    @Column(name = "postal_code",
            nullable = false,
            length = 20
    )
    private String postalCode;

    @Column(name = "label",
            length = 50
    )
    private String label; // e.g., "Home", "Office"

    @Column(name = "is_default",
            nullable = false
    )
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(updatable = false,
            name = "created_at"
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}