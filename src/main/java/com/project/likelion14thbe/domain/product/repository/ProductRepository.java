package com.project.likelion14thbe.domain.product.repository; // 패키지 구조는 프로젝트에 맞게 수정하세요.

import com.project.likelion14thbe.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    List<Product> findAllByDeletedAtIsNull();
}