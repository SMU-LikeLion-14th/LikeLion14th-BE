package com.project.likelion14thbe.domain.product.entity;

import com.project.likelion14thbe.domain.member.entity.BaseEntity;
import com.project.likelion14thbe.domain.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void update(String name, String description, Integer price, String imageUrl, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }
}
