package com.example.pricetag.features.Variant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sizes",
        indexes = {@Index(name = "idx_size_name",
                columnList = "name"
        )}
)
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // XS, S, M, L, 42, 9.5
    @Column(nullable = false,
            length = 20
    )
    private String name;

    // Display order (XS=1, S=2, M=3...)
    @Column(name = "display_order")
    private Integer displayOrder;

    // Optional grouping (SHIRT, SHOE, JEANS)
    @Column(name = "size_type",
            length = 50
    )
    private String sizeType;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "size",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Variants> variants = new ArrayList<>();
}