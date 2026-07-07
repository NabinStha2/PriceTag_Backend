package com.example.pricetag.features.ratingReview.controller;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rating-review")
@RequiredArgsConstructor
public class RatingReviewController {

    private final RatingReviewService ratingReviewService;

    @PostMapping("/add/product/{producId}")
    public ResponseEntity<CommonResponseDto<Void>> createRatingReview(
            @PathVariable(name = "producId")
            Long productId,
            @RequestBody
            RatingReviewRequestDto ratingReviewRequestDto) {
        return ResponseEntity.ok(
                ratingReviewService.createRatingReview(productId, ratingReviewRequestDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<CommonResponseDto<RatingReviewResponseDto>> getRatingReviewById(
            @PathVariable
            Long id) {
        return ResponseEntity.ok(ratingReviewService.getRatingReviewById(id));
    }

    @GetMapping("/user/product/{productId}")
    public ResponseEntity<CommonResponseDto<RatingReviewResponseDto>> getAllRatingReviewByProductIdAndUserId(
            @PathVariable
            Long productId) {
        return ResponseEntity.ok(
                ratingReviewService.getAllRatingReviewByProductIdAndUserId(productId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<CommonResponseDto<List<RatingReviewResponseDto>>> getAllRatingReviewByProductId(
            @PathVariable
            Long productId,
            @RequestParam(name = "page",
                    defaultValue = "1",
                    required = false)
            int page,
            @RequestParam(name = "limit",
                    defaultValue = "5",
                    required = false)
            int limit,
            @RequestParam(name = "sortBy",
                    required = false)
            String sortBy,
            @RequestParam(name = "order",
                    required = false)
            String order

                                                                                                         ) {
        PaginationDto paginationDtoData = PaginationDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();
        return ResponseEntity.ok(
                ratingReviewService.getAllRatingReviewByProductId(productId, paginationDtoData));
    }

    @GetMapping("/user")
    public ResponseEntity<CommonResponseDto<List<RatingReviewResponseDto>>> getAllRatingReviewByUserId(
            @RequestParam(name = "page",
                    defaultValue = "1",
                    required = false)
            int page,
            @RequestParam(name = "limit",
                    defaultValue = "5",
                    required = false)
            int limit,
            @RequestParam(name = "sortBy",
                    required = false)
            String sortBy,
            @RequestParam(name = "order",
                    required = false)
            String order

                                                                                                      ) {
        PaginationDto paginationDtoData = PaginationDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();
        return ResponseEntity.ok(ratingReviewService.getAllRatingReviewByUserId(paginationDtoData));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<CommonResponseDto<Void>> updateRatingReview(
            @PathVariable
            Long id,
            @RequestBody
            RatingReviewRequestDto ratingReviewRequestDto) {
        return ResponseEntity.ok(
                ratingReviewService.updateRatingReview(id, ratingReviewRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponseDto<Void>> deleteRatingReview(
            @PathVariable
            Long id) {
        return ResponseEntity.ok(ratingReviewService.deleteRatingReview(id));
    }

}
