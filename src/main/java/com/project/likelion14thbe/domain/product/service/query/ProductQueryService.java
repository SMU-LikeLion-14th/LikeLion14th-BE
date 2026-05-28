package com.project.likelion14thbe.domain.product.service.query;

import com.project.likelion14thbe.domain.product.dto.response.ProductResDTO;
import java.util.List;

public interface ProductQueryService {
    ProductResDTO.ProductDetailResDto getProductDetail(Long id);
    List<ProductResDTO.ProductPreviewResDto> getProductList();
}