package com.example.pricetag.features.auth.entity;

import com.example.pricetag.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {
    @Column(name = "name",
            nullable = false,
            unique = true
    )
    private String name; // e.g., ROLE_USER, ROLE_ADMIN

}
