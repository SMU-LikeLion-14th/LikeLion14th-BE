package com.project.likelion14thbe.domain.review.service.query;

import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;
import java.util.List;

public interface ReviewQueryService {
    List<ReviewResDTO.ReviewListRes> getReviews(Long productId);
    List<ReviewResDTO.ReviewListRes> getReviewsByProduct(Long productId);
    ReviewResDTO.ReviewDetailRes getReviewDetail(Long productId, Long reviewId);
    List<ReviewResDTO.ReviewListRes> getMyReviews(Long memberId);
}