package com.project.likelion14thbe.global.security.jwt.repository;

import com.project.likelion14thbe.global.security.jwt.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,String> {

    Optional<Token> findByEmail(String email);

    void deleteByEmail(String email);
}
