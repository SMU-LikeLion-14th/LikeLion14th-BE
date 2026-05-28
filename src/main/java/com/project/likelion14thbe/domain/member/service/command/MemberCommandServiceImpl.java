package com.project.likelion14thbe.domain.member.service.command;

import com.project.likelion14thbe.domain.member.converter.MemberConverter;
import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.exception.MemberErrorCode;
import com.project.likelion14thbe.domain.member.exception.MemberException;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public MemberResDTO.MemberCreateResDTO createMember(MemberReqDTO.MemberCreateReqDTO memberCreateReqDTO) {
        Member member = MemberConverter.toMember(memberCreateReqDTO);
        member.updatePassword(passwordEncoder.encode(memberCreateReqDTO.getPassword()));
        memberRepository.save(member);
        return MemberConverter.toMemberCreateResDTO(member);
    }

    @Override
    public MemberResDTO.LoginRes login(MemberReqDTO.LoginReq request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
        }

        Role role = member.getRole() != null ? member.getRole() : Role.ROLE_USER;
        CustomUserDetails userDetails = new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                role
        );

        String accessToken = jwtUtil.createJwtAccessToken(userDetails);
        String refreshToken = jwtUtil.createJwtRefreshToken(userDetails);

        return MemberConverter.toLoginRes(member, accessToken, refreshToken);
    }

    @Override
    public void updatePassword(Long memberId, MemberReqDTO.PasswordResetDTO dto) {
        Member member = memberRepository.findByIdAndNotDeleted(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.updatePassword(passwordEncoder.encode(dto.getPassword()));
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findByIdAndNotDeleted(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.delete();
    }
}