package com.project.likelion14thbe.domain.category.converter;

import com.project.likelion14thbe.domain.category.dto.request.CategoryReqDTO;
import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;
import com.project.likelion14thbe.domain.category.entity.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConverter {

    public static Category toCategory(CategoryReqDTO.CategoryCreateReqDto request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public static CategoryResDTO.CategoryResDto toCategoryResDto(Category category) {
        return CategoryResDTO.CategoryResDto.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .build();
    }
}
