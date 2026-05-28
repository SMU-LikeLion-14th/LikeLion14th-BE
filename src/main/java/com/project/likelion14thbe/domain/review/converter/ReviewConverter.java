package com.project.likelion14thbe.domain.review.converter;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.product.entity.Product;
import com.project.likelion14thbe.domain.review.dto.request.ReviewReqDTO;
import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;
import com.project.likelion14thbe.domain.review.entity.Review;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewConverter {

    // 1. DTO -> Entity 변환: 리뷰 생성
    public static Review toReview(ReviewReqDTO.ReviewCreateReq request, Member member, Product product) {
        return Review.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .rating(request.getRating())
                .member(member)
                .product(product)
                .build();
    }

    // 2. Entity -> Response DTO 변환: 리뷰 목록 조회
    public static ReviewResDTO.ReviewListRes toReviewListRes(Review review) {
        return ReviewResDTO.ReviewListRes.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .rating(review.getRating() != null ? review.getRating().doubleValue() : null)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // 3. Entity -> Response DTO 변환: 리뷰 상세 조회
    public static ReviewResDTO.ReviewDetailRes toReviewDetailRes(Review review) {
        return ReviewResDTO.ReviewDetailRes.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .rating(review.getRating() != null ? review.getRating() : 0.0)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .memberId(review.getMember() != null ? review.getMember().getId() : null)
                .profileImage(review.getMember() != null && review.getMember().getPhotoImg() != null
                        ? review.getMember().getPhotoImg()
                        : "https://image.com/default-profile.jpg")
                .build();
    }

    public static ReviewResDTO.ReviewCreateResDto toReviewCreateResDto(Review review) {
        return ReviewResDTO.ReviewCreateResDto.builder()
                .id(review.getReviewId())
                .title(review.getTitle())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}