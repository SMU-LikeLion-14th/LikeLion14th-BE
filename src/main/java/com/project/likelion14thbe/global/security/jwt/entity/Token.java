package com.project.likelion14thbe.global.security.jwt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "token")
public class Token {

    @Id
    private String email;
    private String token;

}
