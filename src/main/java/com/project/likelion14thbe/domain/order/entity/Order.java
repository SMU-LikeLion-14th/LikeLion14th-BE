package com.project.likelion14thbe.domain.order.entity;

import com.project.likelion14thbe.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import com.project.likelion14thbe.domain.product.entity.Product;
import com.project.likelion14thbe.domain.product.repository.ProductRepository;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Order  {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long orderId;

    @CreatedDate
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "address")
    private String address;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    public void updateStatus(String status) {
        this.status = OrderStatus.valueOf(status);
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}


