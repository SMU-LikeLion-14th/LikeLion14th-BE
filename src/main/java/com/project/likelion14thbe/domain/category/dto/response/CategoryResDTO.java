package com.project.likelion14thbe.domain.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryResDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리 응답 DTO")
    public static class CategoryResDto {

        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;

        @Schema(description = "카테고리 이름", example = "Furniture")
        private String name;
    }
}