package com.project.likelion14thbe.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ProductReqDTO {

    @Getter
    @Setter
    @Schema(description = "상품 목록 필터링 요청 DTO")
    public static class ProductRequestDTO {
        @Schema(description = "필터링할 카테고리", example = "Furniture")
        private String category;
    }

    @Getter
    @Setter
    @Schema(description = "상품 추가 요청 DTO")
    public static class ProductCreateReqDto {
        @Schema(description = "상품 이름", example = "Minimal Stand")
        private String name;

        @Schema(description = "상품 상세 설명", example = "어디에나 잘 어울리는 미니멀한 디자인의 스탠드입니다.")
        private String description;

        @Schema(description = "상품 가격", example = "25000")
        private Integer price;

        @Schema(description = "카테고리", example = "Furniture")
        private String category;

        @Schema(description = "상품 이미지 URL", example = "https://image.com/stand.jpg")
        private String imageUrl;

        @Schema(description = "수량")
        private Integer stock;
    }

    @Getter
    @Setter
    @Schema(description = "상품 수정 요청 DTO")
    public static class ProductUpdateReqDto {
        @Schema(description = "상품 이름", example = "Minimal Stand")
        private String name;

        @Schema(description = "상품 상세 설명", example = "어디에나 잘 어울리는 미니멀한 디자인의 스탠드입니다.")
        private String description;

        @Schema(description = "상품 가격", example = "25000")
        private Integer price;

        @Schema(description = "카테고리", example = "Furniture")
        private String category;

        @Schema(description = "상품 이미지 URL", example = "https://image.com/stand.jpg")
        private String imageUrl;

        @Schema(description = "상품 재고", example = "100")
        private Integer stock;
    }
}