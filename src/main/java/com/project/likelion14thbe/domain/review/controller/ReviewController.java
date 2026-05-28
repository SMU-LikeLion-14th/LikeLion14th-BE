package com.project.likelion14thbe.domain.review.controller;

import com.project.likelion14thbe.domain.review.dto.request.ReviewReqDTO;
import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;
import com.project.likelion14thbe.domain.review.service.command.ReviewCommandService;
import com.project.likelion14thbe.domain.review.service.query.ReviewQueryService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Review", description = "리뷰 관련 API")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("/products/{productId}/reviews")
    @Operation(summary = "리뷰 작성")
    public CustomResponse<ReviewResDTO.ReviewCreateResDto> createReview(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewReqDTO.ReviewCreateReq request
    ) {
        request.setProductId(productId);
        ReviewResDTO.ReviewCreateResDto response = reviewCommandService.createReview(userDetails.getMemberId(), request);
        return CustomResponse.onSuccess(HttpStatus.CREATED, response);
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "리뷰 목록 조회")
    public CustomResponse<List<ReviewResDTO.ReviewListRes>> getReviews(@PathVariable Long productId) {
        return CustomResponse.onSuccess(reviewQueryService.getReviews(productId));
    }

    @GetMapping("/products/{productId}/reviews/{reviewId}")
    @Operation(summary = "단일 리뷰 조회")
    public CustomResponse<ReviewResDTO.ReviewDetailRes> getReviewDetail(
            @PathVariable Long productId,
            @PathVariable Long reviewId
    ) {
        return CustomResponse.onSuccess(reviewQueryService.getReviewDetail(productId,reviewId));
    }

    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정")
    public CustomResponse<ReviewResDTO.ReviewDetailRes> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewReqDTO.ReviewUpdateReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReviewResDTO.ReviewDetailRes response = reviewCommandService.updateReview(reviewId, request, userDetails.getMemberId());
        return CustomResponse.onSuccess(response);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    public CustomResponse<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        reviewCommandService.deleteReview(reviewId, userDetails.getMemberId());
        return CustomResponse.onSuccess("리뷰가 삭제되었습니다.");
    }
}