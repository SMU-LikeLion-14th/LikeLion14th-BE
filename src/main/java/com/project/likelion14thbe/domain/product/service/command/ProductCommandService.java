package com.project.likelion14thbe.domain.product.service.command;

import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import com.project.likelion14thbe.domain.product.dto.request.ProductReqDTO;
import com.project.likelion14thbe.domain.product.dto.response.ProductResDTO;

public interface ProductCommandService {
    ProductResDTO.ProductCreateResDto createProduct(ProductReqDTO.ProductCreateReqDto request);
    void deleteProduct(Long id);
    ProductResDTO.ProductCreateResDto updateProduct(Long id, ProductReqDTO.ProductUpdateReqDto request);
}