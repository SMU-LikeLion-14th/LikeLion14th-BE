package com.project.likelion14thbe.domain.review.service.query;

import com.project.likelion14thbe.domain.review.converter.ReviewConverter;
import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;
import com.project.likelion14thbe.domain.review.entity.Review;
import com.project.likelion14thbe.domain.review.exception.ReviewErrorCode;
import com.project.likelion14thbe.domain.review.exception.ReviewException;
import com.project.likelion14thbe.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewRepository reviewRepository;

    @Override
    public List<ReviewResDTO.ReviewListRes> getReviews(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewConverter::toReviewListRes)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResDTO.ReviewDetailRes getReviewDetail(Long productId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        return ReviewConverter.toReviewDetailRes(review);
    }

    @Override
    public List<ReviewResDTO.ReviewListRes> getMyReviews(Long memberId) {
        return reviewRepository.findByMemberId(memberId).stream()
                .map(ReviewConverter::toReviewListRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResDTO.ReviewListRes> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewConverter::toReviewListRes)
                .collect(Collectors.toList());
    }
}