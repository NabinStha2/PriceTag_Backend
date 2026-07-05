package com.example.pricetag.features.ratingReview.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingReviewServiceImpl implements RatingReviewService {


    @Override
    public CommonResponseDto<RatingReviewResponseDto> createRatingReview(Long productId,
                                                                         RatingReviewRequestDto ratingReviewRequestDto) {

        return null;
    }
}
