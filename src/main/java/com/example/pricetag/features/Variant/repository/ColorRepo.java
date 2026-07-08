package com.example.pricetag.features.Variant.repository;

import com.example.pricetag.features.Variant.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepo extends JpaRepository<Color, Long> {
    Optional<Color> findColorById(Long id);
}

