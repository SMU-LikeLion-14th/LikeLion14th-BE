package com.project.likelion14thbe.domain.product.repository;

import com.project.likelion14thbe.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
