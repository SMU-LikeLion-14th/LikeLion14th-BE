package com.project.likelion14thbe.domain.review.dto.request;

import com.project.likelion14thbe.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ReviewReqDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 작성 요청 DTO")
    public static class ReviewCreateReq {

        @Schema(description = "리뷰를 작성할 상품 ID", example = "1")
        private Long productId;

        @Schema(description = "리뷰제목")
        private String title;

        @Schema(description = "리뷰 내용", example = "이 제품 정말 좋아요 !! 다들 구매하세요 ~!")
        private String content;

        @Schema(description = "평점 (0.0 ~ 5.0)", example = "5.0")
        private Double rating;

        @Schema(description = "작성자아이디")
        private String memberId;

    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 수정 요청 DTO")
    public static class ReviewUpdateReq {

        @Schema(description = "리뷰제목")
        private String title;

        @Schema(description = "리뷰 내용")
        private String content;

        @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5")
        private Double rating;
    }

}