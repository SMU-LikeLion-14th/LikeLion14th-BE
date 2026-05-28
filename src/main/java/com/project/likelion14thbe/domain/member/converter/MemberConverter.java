package com.project.likelion14thbe.domain.member.converter;

import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberConverter {
    public static Member toMember(MemberReqDTO.MemberCreateReqDTO memberCreateReqDTO) {
        return Member.builder()
                .email(memberCreateReqDTO.getEmail())
                .password(memberCreateReqDTO.getPassword())
                .age(Integer.valueOf(memberCreateReqDTO.getAge()))
                .role(Role.ROLE_USER)
                .build();
    }

    public static MemberResDTO.MemberCreateResDTO toMemberCreateResDTO(Member member) {
        return MemberResDTO.MemberCreateResDTO.builder()
                .id(member.getId())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static MemberResDTO.MemberPreviewResDTO toMemberPreviewResDTO(Member member) {
        return MemberResDTO.MemberPreviewResDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .age(member.getAge())
                .build();
    }

    public static MemberResDTO.LoginRes toLoginRes(Member member, String accessToken, String refreshToken) {
        return MemberResDTO.LoginRes.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
