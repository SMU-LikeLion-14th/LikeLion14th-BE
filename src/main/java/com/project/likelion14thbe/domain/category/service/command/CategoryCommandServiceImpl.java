package com.project.likelion14thbe.domain.category.service.command;


import com.project.likelion14thbe.domain.category.converter.CategoryConverter;
import com.project.likelion14thbe.domain.category.dto.request.CategoryReqDTO;
import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;
import com.project.likelion14thbe.domain.category.entity.Category;
import com.project.likelion14thbe.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResDTO.CategoryResDto createCategory(CategoryReqDTO.CategoryCreateReqDto request) {
        Category category = CategoryConverter.toCategory(request);
        Category saved = categoryRepository.save(category);
        return CategoryConverter.toCategoryResDto(saved);
    }
}
