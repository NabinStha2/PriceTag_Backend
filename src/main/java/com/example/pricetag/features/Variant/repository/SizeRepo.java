package com.example.pricetag.features.Variant.repository;

import com.example.pricetag.features.Variant.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepo extends JpaRepository<Size, Long> {
    Optional<Size> findSizeById(Long id);
}

