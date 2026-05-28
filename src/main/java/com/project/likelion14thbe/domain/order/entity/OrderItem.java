package com.project.likelion14thbe.domain.order.entity;

import com.project.likelion14thbe.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_order_id")
    private Integer itemOrderId;

//    @Column(name = "item_count", nullable = false)
//    private Integer itemCount;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id",nullable = false)
    private Product product;



}


