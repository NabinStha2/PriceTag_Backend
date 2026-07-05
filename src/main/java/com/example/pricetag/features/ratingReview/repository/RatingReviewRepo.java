package com.example.pricetag.features.ratingReview.repository;

import com.example.pricetag.features.ratingReview.entity.RatingReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingReviewRepo extends JpaRepository<RatingReview, Long> {
    Page<RatingReview> findAllByProductIdAndIsActiveTrue(Long productId, Pageable pageable);
}
