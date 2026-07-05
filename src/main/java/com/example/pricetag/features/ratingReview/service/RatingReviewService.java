package com.example.pricetag.features.ratingReview.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;

public interface RatingReviewService {
    CommonResponseDto<RatingReviewResponseDto> createRatingReview(
            Long productId, RatingReviewRequestDto ratingReviewRequestDto
                                                                 );

}
