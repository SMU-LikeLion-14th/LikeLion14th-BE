
package com.project.likelion14thbe.domain.category.controller;

import com.project.likelion14thbe.domain.category.dto.request.CategoryReqDTO;
import com.project.likelion14thbe.domain.category.dto.response.CategoryResDTO;
import com.project.likelion14thbe.domain.category.service.command.CategoryCommandService;
import com.project.likelion14thbe.domain.category.service.query.CategoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Category", description = "카테고리 관련 API")
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @PostMapping("")
    @Operation(summary = "카테고리 등록", description = "새로운 카테고리를 등록합니다.")
    public ResponseEntity<CategoryResDTO.CategoryResDto> createCategory(
            @Valid @RequestBody CategoryReqDTO.CategoryCreateReqDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryCommandService.createCategory(request));
    }

    @GetMapping("")
    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    public ResponseEntity<List<CategoryResDTO.CategoryResDto>> getAllCategories() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryQueryService.getCategoryList());
    }
}