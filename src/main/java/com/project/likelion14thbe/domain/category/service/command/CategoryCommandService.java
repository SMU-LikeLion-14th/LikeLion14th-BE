package com.project.likelion14thbe.domain.category.service.command;


import com.project.likelion14thbe.domain.category.dto.request.CategoryReqDTO;
import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;

public interface CategoryCommandService {
    CategoryResDTO.CategoryResDto createCategory(CategoryReqDTO.CategoryCreateReqDto request);
}
