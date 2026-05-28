package com.project.likelion14thbe.domain.member.repository;

import com.project.likelion14thbe.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Member> findByIdAndNotDeleted(@Param("id") Long id);

    Optional<Member> findByEmail(String email);

    // 30일 지난 회원 영구 삭제
    @Modifying
    @Query("DELETE FROM Member m WHERE m.deletedAt IS NOT NULL AND m.deletedAt < :date")
    int deleteByDeletedAtBefore(@Param("date") LocalDateTime date);
}