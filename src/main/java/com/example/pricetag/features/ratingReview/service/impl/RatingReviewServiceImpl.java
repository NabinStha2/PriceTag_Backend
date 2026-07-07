package com.example.pricetag.features.ratingReview.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginatedResponseDto;
import com.example.pricetag.dto.PaginationDto;
import com.example.pricetag.entity.User;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.product.repository.ProductRepo;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.example.pricetag.features.ratingReview.mapper.RatingReviewMapper;
import com.example.pricetag.features.ratingReview.repository.RatingReviewRepo;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import com.example.pricetag.repository.UserRepo;
import com.example.pricetag.utils.ColorLogger;
import com.example.pricetag.utils.PageableBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        ColorLogger.logDebug("createRatingReview : " + newRatingReview);

        return CommonResponseDto
                .<Void>builder()
                .message("Rating and review created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .build();
    }


    @Override
    public CommonResponseDto<RatingReviewResponseDto> getRatingReviewById(Long id) {
        RatingReview existingRatingReview = ratingReviewRepo
                .findById(id)
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
    public CommonResponseDto<RatingReviewResponseDto> getAllRatingReviewByProductIdAndUserId(
            Long productId, Long userId) {
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
    public CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByProductId(
            Long productId, PaginationDto paginationDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationDto);
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
    public CommonResponseDto<List<RatingReviewResponseDto>> getAllRatingReviewByUserId(Long userId,
                                                                                       PaginationDto paginationDto) {
        Pageable pageable = PageableBuilder.buildPageable(paginationDto);
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

}
