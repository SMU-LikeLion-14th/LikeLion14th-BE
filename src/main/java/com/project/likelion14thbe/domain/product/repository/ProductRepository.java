package com.project.likelion14thbe.domain.product.repository;


import com.project.likelion14thbe.domain.member.entity.Member;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{
}
