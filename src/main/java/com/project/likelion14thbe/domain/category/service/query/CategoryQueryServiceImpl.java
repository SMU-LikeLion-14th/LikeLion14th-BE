package com.project.likelion14thbe.domain.category.service.query;

import com.project.likelion14thbe.domain.category.converter.CategoryConverter;
import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;
import com.project.likelion14thbe.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResDTO.CategoryResDto> getCategoryList() {
        return categoryRepository.findAll().stream()
                .map(CategoryConverter::toCategoryResDto)
                .collect(Collectors.toList());
    }
}