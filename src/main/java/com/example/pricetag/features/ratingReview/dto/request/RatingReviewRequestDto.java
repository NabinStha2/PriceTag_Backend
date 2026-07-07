package com.example.pricetag.features.ratingReview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingReviewRequestDto {
    @NotNull(message = "Rating is required")
    @Min(value = 0,
            message = "Rating must be at least 0")
    @Max(value = 5,
            message = "Rating must be at most 5")
    private Double rating;

    @Size(max = 2000,
            message = "Review must be at most 2000 characters")
    private String review;
}
