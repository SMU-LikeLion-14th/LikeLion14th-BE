package com.project.likelion14thbe.domain.member.service.command;

import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberCommandServiceInvalidationTest {

    private MemberRepository memberRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private TokenInvalidationService invalidationService;
    private MemberCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        invalidationService = mock(TokenInvalidationService.class);
        service = new MemberCommandServiceImpl(memberRepository, passwordEncoder, invalidationService);
    }

    @Test
    void updatePassword_invalidatesExistingTokens() {
        Member member = mock(Member.class);
        when(memberRepository.findByEmailAndNotDeleted("kim@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(any())).thenReturn("enc");

        service.updatePassword("kim@test.com", new MemberReqDTO.PasswordResetDTO("newPassw0rd!"));

        verify(invalidationService).invalidateUser("kim@test.com");
    }

    @Test
    void deleteMember_invalidatesExistingTokens() {
        Member member = mock(Member.class);
        when(memberRepository.findByEmailAndNotDeleted("kim@test.com")).thenReturn(Optional.of(member));

        service.deleteMember("kim@test.com");

        verify(invalidationService).invalidateUser("kim@test.com");
    }
}
