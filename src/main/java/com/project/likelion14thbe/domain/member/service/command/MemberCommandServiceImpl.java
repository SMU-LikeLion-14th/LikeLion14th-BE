package com.project.likelion14thbe.domain.member.service.command;

import com.project.likelion14thbe.domain.member.converter.MemberConverter;
import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.exception.MemberErrorCode;
import com.project.likelion14thbe.domain.member.exception.MemberException;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenInvalidationService tokenInvalidationService;

    @Override
    public MemberResDTO.SignUpResDTO signUp(MemberReqDTO.SignUpReqDTO request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATE);
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = MemberConverter.toMember(request, encodedPassword);
        Member saved = memberRepository.save(member);
        return MemberConverter.toSignUpResDTO(saved);
    }

    @Override
    public void updatePassword(String email, MemberReqDTO.PasswordResetDTO dto) {
        Member member = memberRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updatePassword(passwordEncoder.encode(dto.password()));
        tokenInvalidationService.invalidateUser(email);
    }

    @Override
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.softDelete();
        tokenInvalidationService.invalidateUser(email);
    }
}
