package com.project.likelion14thbe.domain.review.service.command;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.exception.MemberErrorCode;
import com.project.likelion14thbe.domain.member.exception.MemberException;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.domain.product.entity.Product;
import com.project.likelion14thbe.domain.product.exception.ProductErrorCode;
import com.project.likelion14thbe.domain.product.exception.ProductException;
import com.project.likelion14thbe.domain.product.repository.ProductRepository;
import com.project.likelion14thbe.domain.review.converter.ReviewConverter;
import com.project.likelion14thbe.domain.review.dto.request.ReviewReqDTO;
import com.project.likelion14thbe.domain.review.dto.response.ReviewResDTO;
import com.project.likelion14thbe.domain.review.entity.Review;
import com.project.likelion14thbe.domain.review.exception.ReviewErrorCode;
import com.project.likelion14thbe.domain.review.exception.ReviewException;
import com.project.likelion14thbe.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Override
    public ReviewResDTO.ReviewCreateResDto createReview(Long memberId, ReviewReqDTO.ReviewCreateReq request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Review review = ReviewConverter.toReview(request, member, product);
        reviewRepository.save(review);
        return ReviewConverter.toReviewCreateResDto(review);
    }

    @Override
    @Transactional
    public ReviewResDTO.ReviewDetailRes updateReview(Long reviewId, ReviewReqDTO.ReviewUpdateReq request, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new ReviewException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        review.update(request.getTitle(), request.getContent(), request.getRating());
        return ReviewConverter.toReviewDetailRes(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new ReviewException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        reviewRepository.delete(review);
    }
}