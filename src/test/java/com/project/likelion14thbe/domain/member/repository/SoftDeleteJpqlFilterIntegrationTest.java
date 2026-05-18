package com.project.likelion14thbe.domain.member.repository;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.support.AbstractDbIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * goal #3 — MemberRepository.findByEmailAndNotDeleted 의 JPQL 이 실행 시 생성하는 SQL 이
 * deleted_at IS NOT NULL 행을 실제로 결과에서 제외하는지 검증한다.
 *
 * <p>목으로는 JPQL→SQL 변환이 일어나지 않아 증명 불가. 실 MySQL 에서 soft-delete 된
 * 행이 실제로 필터링되는지 확인한다.
 *
 * <p>회귀 시뮬레이션 ABC: MemberRepository.findByEmailAndNotDeleted 의 JPQL 에서
 * {@code AND m.deletedAt IS NULL} 을 임시 삭제하면 soft-delete 행이 조회되어 본 테스트가
 * RED 가 되고, 복원 시 GREEN 으로 돌아옴을 독립 재현한다.
 * (무력화 편집은 커밋하지 않는다.)
 */
@SpringBootTest
class SoftDeleteJpqlFilterIntegrationTest extends AbstractDbIntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void cleanUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("soft-delete 된 회원은 findByEmailAndNotDeleted 결과에서 제외되고, 활성 회원은 조회된다")
    void findByEmailAndNotDeleted_excludesSoftDeletedRow() {
        // given: 활성 회원 1명
        memberRepository.saveAndFlush(Member.builder()
                .name("활성")
                .email("active@test.com")
                .password("schema-compat")
                .role(Role.ROLE_USER)
                .build());

        // given: 저장 후 soft-delete 처리한 회원 1명
        Member gone = memberRepository.saveAndFlush(Member.builder()
                .name("탈퇴")
                .email("gone@test.com")
                .password("schema-compat")
                .role(Role.ROLE_USER)
                .build());
        gone.softDelete();
        memberRepository.saveAndFlush(gone);

        // when
        Optional<Member> deletedLookup = memberRepository.findByEmailAndNotDeleted("gone@test.com");
        Optional<Member> activeLookup = memberRepository.findByEmailAndNotDeleted("active@test.com");

        // then: soft-delete 행은 제외, 활성 행은 조회 (사용자 관찰 가능 결과)
        assertThat(deletedLookup).isEmpty();
        assertThat(activeLookup).isPresent();
        assertThat(activeLookup.get().getEmail()).isEqualTo("active@test.com");
    }
}
