package com.project.likelion14thbe.domain.order.repository;


import com.project.likelion14thbe.domain.member.entity.Member;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
