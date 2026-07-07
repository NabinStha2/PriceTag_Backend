package com.example.pricetag.features.ratingReview.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;

import java.util.List;

public interface RatingReviewService {
    CommonResponseDto<Void> createRatingReview(Long productId,
                                               RatingReviewRequestDto ratingReviewRequestDto);

    CommonResponseDto<RatingReviewResponseDto> getRatingReviewById(
            Long id);

    CommonResponseDto<RatingReviewResponseDto> getAllRatingReviewByProductIdAndUserId(
            Long productId, Long userId);


    CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByProductId(Long productId,
                                                                                   PaginationDto paginationDto);

    CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByUserId(Long userId,
                                                                                PaginationDto paginationDto);

}
