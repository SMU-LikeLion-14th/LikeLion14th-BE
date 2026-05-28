package com.project.likelion14thbe.domain.product.dto.response;

import com.project.likelion14thbe.domain.product.dto.request.ProductReqDTO;
import com.project.likelion14thbe.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 생성 응답 DTO")
    public static class ProductCreateResDto {

        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 목록 응답 DTO")
    public static class ProductResponseDTO {

        @Schema(description = "상품 고유 ID", example = "1")
        private Long productId;

        @Schema(description = "상품 이름", example = "Minimal Stand")
        private String name;

        @Schema(description = "상품 가격", example = "25000")
        private Integer price;

        @Schema(description = "상품 대표 이미지 URL", example = "https://image.com/stand.jpg")
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 상세 조회 응답 DTO")
    public static class ProductDetailResDto {
        @Schema(description = "상품 고유 ID")
        private Long productId;

        @Schema(description = "상품 이름")
        private String name;

        @Schema(description = "상품 상세 설명")
        private String description;

        @Schema(description = "상품 가격" )
        private Integer price;

        @Schema(description = "카테고리")
        private String category;

        @Schema(description = "상품 이미지 URL")
        private String imageUrl;

        @Schema(description = "상품 이미지(photoImg)")
        private String photoImg;

        @Schema(description = "상품 재고(stock)")
        private Integer stock;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 목록 미리보기용 응답 DTO")
    public static class ProductPreviewResDto {

        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "상품명", example = "멋사 후드티")
        private String name;

        @Schema(description = "상품가격", example = "30000")
        private Long price;

        @Schema(description = "상품이미지", example = "image_url.jpg")
        private String photoImg;

        @Schema(description = "상품재고", example = "100")
        private Integer stock;
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 목록 조회 최종 응답 DTO")
    public static class ProductListResDto {

        @Schema(description = "상품 목록")
        private List<ProductResponseDTO> productList;

        @Schema(description = "조회된 총 상품 수")
        private Integer totalCount;

    }

}