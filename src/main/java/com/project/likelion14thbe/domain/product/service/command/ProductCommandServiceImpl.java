package com.project.likelion14thbe.domain.product.service.command;

import com.project.likelion14thbe.domain.category.entity.Category;
import com.project.likelion14thbe.domain.category.exception.CategoryErrorCode;
import com.project.likelion14thbe.domain.category.exception.CategoryException;
import com.project.likelion14thbe.domain.product.converter.ProductConverter;
import com.project.likelion14thbe.domain.product.dto.request.ProductReqDTO;
import com.project.likelion14thbe.domain.product.dto.response.ProductResDTO;
import com.project.likelion14thbe.domain.product.entity.Product;
import com.project.likelion14thbe.domain.product.exception.ProductErrorCode;
import com.project.likelion14thbe.domain.product.exception.ProductException;
import com.project.likelion14thbe.domain.product.repository.CategoryRepository;
import com.project.likelion14thbe.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.likelion14thbe.domain.product.converter.ProductConverter.toProductCreateResDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResDTO.ProductCreateResDto createProduct(ProductReqDTO.ProductCreateReqDto request) {
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        Product product = ProductConverter.toProduct(request, category);
        Product savedProduct = productRepository.save(product);
        return toProductCreateResDTO(savedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.delete();
        productRepository.save(product);
    }

    @Override
    public ProductResDTO.ProductCreateResDto updateProduct(Long id, ProductReqDTO.ProductUpdateReqDto request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Category category = null;
        if (request.getCategory() != null) {
            category = categoryRepository.findByName(request.getCategory())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        }

        ProductConverter.updateProduct(product, request, category);

        Product savedProduct = productRepository.save(product);
        return toProductCreateResDTO(savedProduct);
    }
}