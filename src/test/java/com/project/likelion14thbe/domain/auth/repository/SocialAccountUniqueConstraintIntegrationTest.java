package com.project.likelion14thbe.domain.auth.repository;

import com.project.likelion14thbe.domain.auth.entity.SocialAccount;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.support.AbstractDbIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * goal #1 — social_account 의 uk_social_provider_provider_id (provider, provider_id)
 * 유니크 제약이 동일 소셜 계정 2회차 가입을 실제 MySQL 레벨에서 차단하는지 검증한다.
 *
 * <p>목으로는 증명 불가(목 리포지토리는 제약을 강제하지 않음). 실 MySQL + 엔티티 기반
 * 스키마 생성으로 제약이 실제로 INSERT 를 거부하는지 확인한다.
 *
 * <p>회귀 시뮬레이션 ABC: {@code SocialAccount} 의 {@code @Table(uniqueConstraints=...)}
 * 속성을 임시 제거하면 두 번째 saveAndFlush 가 성공하여 본 테스트가 RED 가 되고,
 * byte-identical 복원 시 GREEN 으로 돌아옴을 개발/리뷰에서 독립 재현한다.
 * (무력화 편집은 커밋하지 않는다.)
 */
@SpringBootTest
class SocialAccountUniqueConstraintIntegrationTest extends AbstractDbIntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialAccountRepository socialAccountRepository;

    @AfterEach
    void cleanUp() {
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 (provider, providerId) 소셜 계정 2건 저장 시 두 번째는 DB 제약으로 거부된다")
    void duplicateSocialAccount_isRejectedByUniqueConstraint() {
        // given: 활성 회원 1명과 그 회원의 카카오 소셜 계정 1건 영속화
        Member member = memberRepository.saveAndFlush(Member.builder()
                .name("규언")
                .email("uniq@kakao.com")
                .password("schema-compat")
                .role(Role.ROLE_USER)
                .build());

        socialAccountRepository.saveAndFlush(SocialAccount.builder()
                .provider(Provider.KAKAO)
                .providerId("dup-1")
                .member(member)
                .build());

        // when: 동일 (provider, providerId) 로 두 번째 소셜 계정 저장 시도
        SocialAccount duplicate = SocialAccount.builder()
                .provider(Provider.KAKAO)
                .providerId("dup-1")
                .member(member)
                .build();

        // then: DB 유니크 제약이 INSERT 를 거부하여 예외가 발생한다
        assertThatThrownBy(() -> socialAccountRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 그리고 첫 번째 1건만 영속화되어 있다 (관찰 가능 결과)
        assertThat(socialAccountRepository.count()).isEqualTo(1);
    }
}
