package com.project.likelion14thbe.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReviewResDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewCreateResDto {

        @Schema(description = "리뷰아이디")
        private Long id;

        @Schema(description = "제목")
        private String title;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 목록 조회 응답 DTO")
    public static class ReviewListRes {

        @Schema(description = "리뷰 고유 ID", example = "123")
        private Long reviewId;

        @Schema(description = "리뷰 제목")
        private String title;

        @Schema(description = "평점", example = "5")
        private Double rating;

        @Schema(description = "리뷰 내용", example = "이 제품 정말 좋아요 !! 다들 구매하세요 ~!")
        private String content;

        @Schema(description = "작성 일시")
        private LocalDateTime createdAt;

        @Schema(description = "작성자 닉네임", example = "Bruno Fernandes")
        private Long  memberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "단일/세부 리뷰 조회 응답 DTO")
    public static class ReviewDetailRes {

        @Schema(description = "리뷰 고유 ID", example = "123")
        private Long reviewId;

        @Schema(description = "리뷰제목")
        private String title;

        @Schema(description = "평점", example = "5")
        private Double rating;

        @Schema(description = "리뷰 내용", example = "이 제품 정말 좋아요 !! 다들 구매하세요 ~!")
        private String content;

        @Schema(description = "작성 일시", example = "2026-04-30T15:48:09.025Z")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시", example = "2026-04-30T15:48:09.025Z")
        private LocalDateTime updatedAt;

        @Schema(description = "작성자 닉네임", example = "Bruno Fernandes")
        private Long  memberId;

        @Schema(description = "작성자 프로필 이미지 URL", example = "http~")
        private String profileImage;
    }
}