package com.project.likelion14thbe.domain.member.service.command;

import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;

public interface MemberCommandService {

    MemberResDTO.MemberCreateResDTO createMember(MemberReqDTO.MemberCreateReqDTO memberCreateReqDTO);

    MemberResDTO.LoginRes login(MemberReqDTO.LoginReq request);

    public void deleteMember(Long memberId);

    void updatePassword(Long memberId, MemberReqDTO.PasswordResetDTO request);

}
