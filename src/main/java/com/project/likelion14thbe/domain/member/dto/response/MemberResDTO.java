package com.project.likelion14thbe.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MemberResDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 응답 DTO")
    public static class LoginRes {

        @Schema(description = "아이디")
        private Long memberId;

        @Schema(description = "이메일")
        private String email;

        @Schema(description = "액세스 토큰")
        private String accessToken;

        @Schema(description = "리프레시 토큰")
        private String refreshToken;

        @Schema(description = "사용자 이름")
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "회원가입 등 생성관련 응답 DTO")
    public static class MemberCreateResDTO {

        @Schema(description = "아이디")
        private Long id;

        @Schema(description = "생성시간")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "회원목록이나 미리보기용 응답 DTO")
    public static class MemberPreviewResDTO {
        private Long id;
        private String email;
        private Integer age;
    }

}

