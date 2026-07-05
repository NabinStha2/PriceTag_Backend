package com.example.pricetag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "colors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hex_code", "name"}),
        indexes = {@Index(columnList = "hex_code",
                name = "idx_hex_code"
        ), @Index(columnList = "name",
                name = "idx_name"
        )}
)
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",
            nullable = false
    )
    private String name;

    @Column(name = "hex_code",
            nullable = false
    )
    private String hexCode;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "color",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Variants> variants = new ArrayList<>();
}

