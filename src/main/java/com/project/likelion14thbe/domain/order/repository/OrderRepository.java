package com.project.likelion14thbe.domain.order.repository;

import com.project.likelion14thbe.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberId(Long memberId);
}
