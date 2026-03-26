package com.project.likelion14thbe.domain.member.repository;

import com.project.likelion14thbe.domain.member.entity.Member;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository <Member, Long> {
}
