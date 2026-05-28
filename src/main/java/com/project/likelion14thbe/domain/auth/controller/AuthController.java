package com.project.likelion14thbe.domain.auth.controller;

import com.project.likelion14thbe.domain.auth.service.AuthService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "토큰 발급 API", description = "토큰 발급 API입니다.")
public class AuthController {

    private final AuthService authService;

    //토큰 재발급 API
    @Operation(method = "POST", summary = "토큰 재발급", description = "토큰 재발급. accessToken과 refreshToken을 body에 담아서 전송합니다.")
    @PostMapping("/reissue")
    public CustomResponse<?> reissue(@RequestBody JwtDTO jwtDto) {

        log.info("[ Auth Controller ] 토큰을 재발급합니다. ");

        return CustomResponse.onSuccess(authService.reissueToken(jwtDto));
    }

    // 로그아웃 API (Swagger 문서화 전용 - 실제 처리는 Security LogoutFilter)
    @Operation(
            summary = "로그아웃",
            description = "Authorization 헤더에 Bearer {accessToken} 을 담아 호출합니다. " +
                    "DB의 Refresh Token 을 삭제하고, Access Token 을 블랙리스트에 등록하며 SecurityContext 를 클리어합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @PostMapping("/logout")
    public CustomResponse<String> logout() {
        // LogoutFilter 가 이 메서드 호출 전에 가로채므로 실제로 실행되지 않음
        return CustomResponse.onSuccess("로그아웃이 완료되었습니다.");
    }
}
