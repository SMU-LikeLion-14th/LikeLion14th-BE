package com.project.likelion14thbe.domain.review.entity;

import com.project.likelion14thbe.domain.member.entity.BaseEntity;
import com.project.likelion14thbe.domain.product.entity.Product;
import com.project.likelion14thbe.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = true)
    private String content;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Product product;

    @Schema(name = "update")
    public void update(String title, String content, Double rating) {
        this.title = title;
        this.content = content;
        this.rating = rating;
    }

}