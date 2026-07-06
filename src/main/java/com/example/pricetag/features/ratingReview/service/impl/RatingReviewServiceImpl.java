package com.example.pricetag.features.ratingReview.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.entity.User;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.example.pricetag.features.ratingReview.mapper.RatingReviewMapper;
import com.example.pricetag.features.ratingReview.repository.RatingReviewRepo;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import com.example.pricetag.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingReviewServiceImpl implements RatingReviewService {
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final RatingReviewRepo ratingReviewRepo;
    private final RatingReviewMapper ratingReviewMapper;


    @Override
    public CommonResponseDto<Void> createRatingReview(Long productId,
                                                      RatingReviewRequestDto ratingReviewRequestDto) {
        Product existingProduct = productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));
        User existingUser = userRepo
                .findById(ratingReviewRequestDto.getUserId())
                .orElseThrow(() -> new ApplicationException("404", "User not found",
                                                            HttpStatus.NOT_FOUND));

        RatingReview newRatingReview = ratingReviewMapper.mapRatingReviewRequestDtoToRatingReview(
                ratingReviewRequestDto, existingProduct, existingUser);

        ratingReviewRepo.save(newRatingReview);

        return CommonResponseDto
                .<Void>builder()
                .message("Rating and review created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .build();
    }

    @Override
    public CommonResponseDto<Void> getAllRatingReviewByProductIdAndUserId(Long productId,
                                                                          Long userId) {
        Optional<RatingReview> ratingReviewsPageable = ratingReviewRepo.findAllByProductIdAndUserIdAndIsActiveTrue(
                productId, userId);

        return null;
    }
}
