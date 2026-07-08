package com.example.pricetag.features.Variant.repository;


import com.example.pricetag.features.Variant.entity.Variants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepo extends JpaRepository<Variants, Long> {
    boolean existsBySku(String sku);

    List<Variants> findAllByIsActiveTrue();

    Optional<Variants> findByIdAndIsActiveTrue(Long id);

    List<Variants> findAllByProductIdAndIsActiveTrue(Long productId);
}
