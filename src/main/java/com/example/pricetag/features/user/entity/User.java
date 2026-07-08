package com.example.pricetag.features.user.entity;

import com.example.pricetag.entity.Address;
import com.example.pricetag.entity.Cart;
import com.example.pricetag.entity.Order;
import com.example.pricetag.features.auth.entity.Role;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Entity
@Table(name = "users",
        indexes = {@Index(columnList = "email",
                name = "idx_user_email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name",
            length = 100,
            nullable = false)
    private String name;

    @Column(name = "email",
            nullable = false,
            unique = true)
    private String email;

    @Column(name = "password",
            nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "phoneNumber",
            nullable = true)
    private String phoneNumber;

//    @Enumerated(EnumType.STRING)
//    private AppUserRole appUserRole;


    @Column(name = "is_verified",
            nullable = false)
    @Builder.Default
    private boolean isVerified = false;

//    @OneToMany(mappedBy = "user",
//               cascade = CascadeType.ALL,
//               orphanRemoval = true,
//               fetch = FetchType.LAZY
//    )
//    private List<CartItem> cartItems;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<RatingReview> ratingReviews = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Cart cart;

    @Column(name = "is_active",
            nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted",
            nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false,
            name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

}
