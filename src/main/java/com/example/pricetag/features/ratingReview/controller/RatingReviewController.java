package com.example.pricetag.features.ratingReview.controller;


import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.service.RatingReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rating-review")
@RequiredArgsConstructor
public class RatingReviewController {

    private final RatingReviewService ratingReviewService;

    @PostMapping("/product/{producId}")
    public ResponseEntity<CommonResponseDto<Void>> createRatingReview(
            @PathVariable(name = "producId")
            Long productId,
            @RequestBody
            RatingReviewRequestDto ratingReviewRequestDto) {
        return ResponseEntity.ok(ratingReviewService.createRatingReview(
                productId, ratingReviewRequestDto
                                                                       ));
    }

    @GetMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<CommonResponseDto<RatingReviewResponseDto>> getAllRatingReviewByProductIdAndUserId(
            @PathVariable(name = "userId")
            Long userId,
            @PathVariable(name = "productId")
            Long productId) {
        return ResponseEntity.ok(null);
    }


}
