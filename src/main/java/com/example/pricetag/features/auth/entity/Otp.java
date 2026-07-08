package com.example.pricetag.features.auth.entity;

import com.example.pricetag.features.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "otp_code",
            length = 4)
    private int otpCode;

    @CreationTimestamp
    @Column(updatable = false,
            name = "otp_generation_time")
    private LocalDateTime otpGenerationTime;

    @OneToOne
    @JoinColumn(name = "user_id",
            referencedColumnName = "id")
    private User user;

}
