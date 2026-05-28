package com.project.likelion14thbe.domain.category.service.query;


import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;

import java.util.List;

public interface CategoryQueryService {
    List<CategoryResDTO.CategoryResDto> getCategoryList();
}
