package com.project.likelion14thbe.domain.member.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
}
