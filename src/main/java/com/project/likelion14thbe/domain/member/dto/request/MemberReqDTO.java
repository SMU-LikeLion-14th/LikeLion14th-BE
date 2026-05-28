package com.project.likelion14thbe.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.service.annotation.GetExchange;

public class MemberReqDTO {

    @Getter
    @Setter
    @Schema(description = "로그인 요청 DTO")
    public static class LoginReq {
        @NotBlank(message = "이메일은 필수 입력 항목입니다")
        @Schema(description = "이메일", example = "eunseo@sangmyung.ac.kr")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Schema(description = "비밀번호", example = "password123!")
        private String password;
    }

    @Getter
    @Setter
    @Schema(description = "회원가입 요청 DTO")
    public static class MemberCreateReqDTO {
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Schema(description = "이메일", example = "eunseo@sangmyung.ac.kr")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        @Schema(description = "비밀번호", example = "password123!")
        private String password;

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        @Schema(description = "이름", example = "최은서")
        private String name;

        @NotBlank(message = "나이는 필수 입력 항목입니다.")
        @Schema(description = "나이", example = "14")
        private String age;
    }

    @Getter
    @Setter
    public static class PasswordResetDTO {
        @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
        private String password;
    }

}

