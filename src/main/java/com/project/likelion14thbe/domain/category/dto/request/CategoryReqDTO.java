package com.project.likelion14thbe.domain.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CategoryReqDTO {

    @Getter
    @Setter
    @Schema(description = "카테고리 생성 요청 DTO")
    public static class CategoryCreateReqDto {

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Schema(description = "카테고리 이름", example = "Furniture")
        private String name;
    }
}