package com.example.pricetag.features.ratingReview.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingReviewResponseDto {
    private Long id;
    private Long userId;
    private Long productId;
    private Double rating;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userFullName;
    private String productName;
}
