package com.example.pricetag.features.ratingReview.mapper;


import com.example.pricetag.features.product.entity.Product;
import com.example.pricetag.features.ratingReview.dto.request.RatingReviewRequestDto;
import com.example.pricetag.features.ratingReview.dto.response.RatingReviewResponseDto;
import com.example.pricetag.features.ratingReview.entity.RatingReview;
import com.example.pricetag.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING
//        unmappedTargetPolicy = ReportingPolicy.IGNORE,
//        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RatingReviewMapper {
    @Mapping(target = "id",
            ignore = true)
    @Mapping(target = "isActive",
            constant = "true")
    @Mapping(target = "isDeleted",
            constant = "false")
    @Mapping(target = "product",
            source = "product")
    @Mapping(target = "user",
            source = "user")
    @Mapping(target = "createdAt",
            ignore = true)
    @Mapping(target = "updatedAt",
            ignore = true)
    RatingReview mapRatingReviewRequestDtoToRatingReview(
            RatingReviewRequestDto ratingReviewRequestDto, Product product, User user);

    @Mapping(source = "user.id",
            target = "userId")
    @Mapping(source = "product.id",
            target = "productId")
    @Mapping(source = "user.name",
            target = "userFullName")
    @Mapping(source = "product.name",
            target = "productName")
    RatingReviewResponseDto mapRatingReviewToRatingReviewResponseDto(RatingReview ratingReview);


}
