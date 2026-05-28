package com.project.likelion14thbe.domain.review.service.command;

import com.project.likelion14thbe.domain.review.dto.request.ReviewReqDTO;
import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;

public interface ReviewCommandService {
    ReviewResDTO.ReviewCreateResDto createReview(Long memberId, ReviewReqDTO.ReviewCreateReq request);
    void deleteReview(Long id,Long memberId);
    ReviewResDTO.ReviewDetailRes updateReview(Long reviewId, ReviewReqDTO.ReviewUpdateReq request, Long memberId);
}