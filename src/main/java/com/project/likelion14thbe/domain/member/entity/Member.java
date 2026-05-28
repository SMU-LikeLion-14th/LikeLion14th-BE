package com.project.likelion14thbe.domain.member.entity;

import com.project.likelion14thbe.domain.member.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키 값을 자동으로 생성
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column( name = "password", nullable = false)
    private String password;

    @Column(name = "photo_img", nullable = true)
    private String photoImg;

    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @Column(name = "age",nullable = true)
    private Integer age;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete(){this.deletedAt = LocalDateTime.now();}


    public void updatePassword(String newPassword) {this.password = newPassword;}
}



