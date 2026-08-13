package com.example.pricetag.features.Variant.repository;


import com.example.pricetag.features.Variant.entity.Variants;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepo extends JpaRepository<Variants, Long> {
    boolean existsBySku(String sku);

    List<Variants> findAllByIsActiveTrue();

    Optional<Variants> findByIdAndIsActiveTrue(Long id);

    List<Variants> findAllByProductIdAndIsActiveTrue(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
              select distinct v
              from Variants v
              join fetch v.product
              left join fetch v.size
              left join fetch v.color
              where v.id in :variantIds
              order by v.id
            """)
    List<Variants> findAllByIdForUpdate(
            @Param("variantIds")
            List<Long> variantIds);
}
