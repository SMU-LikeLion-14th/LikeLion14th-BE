package com.project.likelion14thbe.domain.review.repository;


import com.project.likelion14thbe.domain.member.entity.Member;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository extends JpaRepository<Member,Long> {
}
