package com.example.pricetag.features.ratingReview.controller;


import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rating-review")
public class RatingReviewController {

    @PostMapping("/product/{producId}")
    public ResponseEntity<Void> createRatingReview(
            @PathVariable(name = "producId")
            Long productId,
            @RequestBody
            RatingReviewRequestDto ratingReviewRequestDto
                                                  ) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/")
    public ResponseEntity<Void> getRatingReviewByProductId() {
        return ResponseEntity.ok(null);
    }
}
