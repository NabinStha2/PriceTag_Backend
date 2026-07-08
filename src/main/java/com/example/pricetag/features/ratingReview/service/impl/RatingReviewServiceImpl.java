package com.example.pricetag.features.ratingReview.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginatedResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.example.pricetag.features.ratingReview.mapper.RatingReviewMapper;
import com.example.pricetag.features.ratingReview.repository.RatingReviewRepo;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import com.example.pricetag.utils.ColorLogger;
import com.example.pricetag.utils.PageableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingReviewServiceImpl implements RatingReviewService {
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final RatingReviewRepo ratingReviewRepo;
    private final RatingReviewMapper ratingReviewMapper;
    private final AuthService authService;


    @Override
    public CommonResponseDto<Void> createRatingReview(Long productId,
                                                      RatingReviewRequestDto ratingReviewRequestDto) {
        Product existingProduct = productRepo
                .findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ApplicationException("404", "Product not found",
                                                            HttpStatus.NOT_FOUND));

        Long userId = authService.getCurrentUserId();

        boolean alreadyReviewed = ratingReviewRepo.existsByProductIdAndUserIdAndIsActiveTrue(
                productId, userId);

        if (alreadyReviewed) {
            throw new ApplicationException("400", "User has already reviewed this product",
                                           HttpStatus.BAD_REQUEST);
        }

        ColorLogger.logInfo("createRatingReview : " + userId + " " + productId);

        User existingUser = userRepo
                .findById(userId)
                .orElseThrow(() -> new ApplicationException("404", "User not found",
                                                            HttpStatus.NOT_FOUND));

        RatingReview newRatingReview = ratingReviewMapper.mapRatingReviewRequestDtoToRatingReview(
                ratingReviewRequestDto, existingProduct, existingUser);

        ratingReviewRepo.save(newRatingReview);

        ColorLogger.logDebug("createRatingReview : " + newRatingReview);

        return CommonResponseDto
                .<Void>builder()
                .message("Rating and review created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<RatingReviewResponseDto> getRatingReviewById(Long id) {
        RatingReview existingRatingReview = ratingReviewRepo
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("404", "Rating and Review Not Found",
                                                            HttpStatus.NOT_FOUND));

        RatingReviewResponseDto ratingReviewResponseDto = ratingReviewMapper.mapRatingReviewToRatingReviewResponseDto(
                existingRatingReview);

        return CommonResponseDto
                .<RatingReviewResponseDto>builder()
                .data(ratingReviewResponseDto)
                .message("Rating Review fetched by id")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<RatingReviewResponseDto> getAllRatingReviewByProductIdAndUserId(
            Long productId) {
        Long userId = authService.getCurrentUserId();
        RatingReview existingRatingReview = ratingReviewRepo
                .findAllByProductIdAndUserIdAndIsActiveTrue(productId, userId)
                .orElseThrow(
                        () -> new ApplicationException("404", "Not Found", HttpStatus.NOT_FOUND));

        RatingReviewResponseDto ratingReviewResponseDto = ratingReviewMapper.mapRatingReviewToRatingReviewResponseDto(
                existingRatingReview);

        return CommonResponseDto
                .<RatingReviewResponseDto>builder()
                .data(ratingReviewResponseDto)
                .message("Rating Review fetched by product and user")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByProductId(
            Long productId, PaginationRequestDto paginationRequestDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationRequestDto);
        Page<RatingReview> pageableRatingReview = ratingReviewRepo.findAllByProductIdAndIsActiveTrue(
                productId, pageable);
        Page<RatingReviewResponseDto> pageableRatingReviewResponseDto = pageableRatingReview.map(
                ratingReviewMapper::mapRatingReviewToRatingReviewResponseDto);

        ColorLogger.logInfo(
                "getAllRatingReviewByProductId : " + pageableRatingReviewResponseDto.getContent());

        return CommonResponseDto
                .<List<RatingReviewResponseDto>>builder()
                .message("Rating review all fetched successfully.")
                .data(pageableRatingReviewResponseDto.getContent())
                .success(true)
                .status(HttpStatus.OK.value())
                .pagination(PaginatedResponseDto.from(pageableRatingReviewResponseDto))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByUserId(
            PaginationRequestDto paginationRequestDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationRequestDto);

        Long userId = authService.getCurrentUserId();

        Page<RatingReview> pageableRatingReview = ratingReviewRepo.findAllByUserIdAndIsActiveTrue(
                userId, pageable);

        Page<RatingReviewResponseDto> pageableRatingReviewResponseDto = pageableRatingReview.map(
                ratingReviewMapper::mapRatingReviewToRatingReviewResponseDto);

        ColorLogger.logInfo(
                "getAllRatingReviewByUserId : " + pageableRatingReviewResponseDto.getContent());

        return CommonResponseDto
                .<List<RatingReviewResponseDto>>builder()
                .message("Rating review all fetched successfully.")
                .data(pageableRatingReviewResponseDto.getContent())
                .success(true)
                .status(HttpStatus.OK.value())
                .pagination(PaginatedResponseDto.from(pageableRatingReviewResponseDto))
                .build();
    }

    @Override
    public CommonResponseDto<Void> updateRatingReview(Long id,
                                                      RatingReviewRequestDto ratingReviewRequestDto) {
        RatingReview existingRatingReview = ratingReviewRepo
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("404", "Rating Review not found",
                                                            HttpStatus.NOT_FOUND));

        Long userId = authService.getCurrentUserId();

        if (!Objects.equals(userId, existingRatingReview
                .getUser()
                .getId())) {
            throw new ApplicationException("403",
                                           "You are not authorized to update this rating review",
                                           HttpStatus.UNAUTHORIZED);
        }

        if (ratingReviewRequestDto.getRating() != null) {
            existingRatingReview.setRating(ratingReviewRequestDto.getRating());
        }
        if (ratingReviewRequestDto.getReview() != null) {
            existingRatingReview.setReview(ratingReviewRequestDto.getReview());
        }

        RatingReviewResponseDto updatedRatingReview = ratingReviewMapper.mapRatingReviewToRatingReviewResponseDto(
                existingRatingReview);


        ColorLogger.logDebug("updatedRatingReview : " + updatedRatingReview);

        return CommonResponseDto
                .<Void>builder()
                .message("Rating and review updated successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();
    }

    @Override
    public CommonResponseDto<Void> deleteRatingReview(Long id) {
        RatingReview existingRatingReview = ratingReviewRepo
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("404", "Rating Review not found",
                                                            HttpStatus.NOT_FOUND));
        Long userId = authService.getCurrentUserId();
        if (!Objects.equals(userId, existingRatingReview
                .getUser()
                .getId())) {
            throw new ApplicationException("400",
                                           "You are not authorized to delete this rating review",
                                           HttpStatus.BAD_REQUEST);
        }


        existingRatingReview.setIsActive(false);
        existingRatingReview.setDeleted(true);

        return CommonResponseDto
                .<Void>builder()
                .message("Rating and review deleted successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();
    }


}
