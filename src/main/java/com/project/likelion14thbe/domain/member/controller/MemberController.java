package com.project.likelion14thbe.domain.member.controller;

import com.project.likelion14thbe.domain.member.dto.request.MemberReqDTO;
import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import com.project.likelion14thbe.domain.member.service.command.MemberCommandService;
import com.project.likelion14thbe.domain.member.service.query.MemberQueryService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Member", description = "회원 및 마이페이지 관련 API")
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public CustomResponse<MemberResDTO.MemberCreateResDTO> signUp(@Valid @RequestBody MemberReqDTO.MemberCreateReqDTO request) {
         return CustomResponse.onSuccess(HttpStatus.CREATED, memberCommandService.createMember(request));
    }

    @PostMapping("/login")
    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인합니다. (실제 처리는 SecurityFilter에서 수행됨)")
    public CustomResponse<MemberResDTO.LoginRes> login(@Valid @RequestBody MemberReqDTO.LoginReq request) {
        return CustomResponse.onSuccess(null);
    }

    @GetMapping("/my")
    @Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
    public CustomResponse<MemberResDTO.MemberPreviewResDTO> getMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("dfdfdfdfdfdfdfdfdfdf{}", customUserDetails.getMemberId());
        return CustomResponse.onSuccess(memberQueryService.getMyInfo(customUserDetails.getMemberId()));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 회원의 비밀번호를 변경합니다.")
    public CustomResponse<String> resetPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberReqDTO.PasswordResetDTO request
    ) {
        memberCommandService.updatePassword(userDetails.getMemberId(), request);
        return CustomResponse.onSuccess("비밀번호 변경 성공");
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    public CustomResponse<String> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        memberCommandService.deleteMember(userDetails.getMemberId());
        return CustomResponse.onSuccess("회원 탈퇴 성공");
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    public CustomResponse<MemberResDTO.MemberPreviewResDTO> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberResDTO.MemberPreviewResDTO myInfo = memberQueryService.getMyInfo(userDetails.getMemberId());
        return CustomResponse.onSuccess(myInfo);
    }
}