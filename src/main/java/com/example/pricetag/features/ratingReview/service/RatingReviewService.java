package com.example.pricetag.features.ratingReview.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;

public interface RatingReviewService {
    CommonResponseDto<Void> createRatingReview(
            Long productId, RatingReviewRequestDto ratingReviewRequestDto
                                              );

    CommonResponseDto<Void> getAllRatingReviewByProductIdAndUserId(
            Long productId, Long userId
                                                                  );

}
