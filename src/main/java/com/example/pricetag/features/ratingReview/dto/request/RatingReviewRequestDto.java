package com.example.pricetag.features.ratingReview.dto.request;

import com.example.pricetag.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingReviewRequestDto {
//    @NotBlank(message = "Product ID is required")
//    private Long productId;

    @NotBlank(message = "User ID is required")
    private User userId;

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
