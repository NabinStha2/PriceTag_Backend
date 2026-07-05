package com.example.pricetag.features.ratingReview.repository;

import com.example.pricetag.features.ratingReview.entity.RatingReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingReviewRepo extends JpaRepository<RatingReview, Long> {
    Page<RatingReview> findAllByProductIdAndIsActiveTrue(Long productId, Pageable pageable);

    Page<RatingReview> findAllByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    Optional<RatingReview> findByIdAndIsActiveTrue(Long id);

    boolean existsByProductIdAndUserIdAndIsActiveTrue(Long productId, Long userId);

    Optional<RatingReview> findAllByProductIdAndUserIdAndIsActiveTrue(Long productId, Long userId);
}
