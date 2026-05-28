package com.project.likelion14thbe.domain.naverlogin.controller;

import com.project.likelion14thbe.domain.naverlogin.dto.NaverTokenResponseDTO;
import com.project.likelion14thbe.domain.naverlogin.dto.NaverUserInfoResponseDTO;
import com.project.likelion14thbe.domain.naverlogin.service.NaverService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@Tag(name = "Naver Login", description = "네이버 소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/auth/naver")
@RequiredArgsConstructor
public class NaverLoginController {

    private final NaverService naverService;

    @Operation(summary = "네이버 로그인 페이지로 redirect",
            description = "호출 시 백엔드에서 네이버 로그인 인증 페이지로 302 redirect 처리합니다.")
    @GetMapping
    public void redirectToNaver(HttpServletResponse response) throws IOException {
        String authorizationUrl = naverService.getAuthorizationUrl();
        log.info("[NaverLoginController] 네이버 로그인 페이지로 redirect: {}", authorizationUrl);
        response.sendRedirect(authorizationUrl);
    }

    @Operation(summary = "네이버 로그인 콜백 (회원가입/로그인 처리)")
    @GetMapping("/callback")
    public CustomResponse<JwtDTO> callback(@RequestParam("code") String code,
                                           @RequestParam("state") String state) {
        log.info("[NaverLoginController] 콜백 진입: code={}, state={}", code, state);

        NaverTokenResponseDTO token = naverService.getAccessToken(code, state);
        NaverUserInfoResponseDTO userInfo = naverService.getUserInfo(token.getAccessToken());
        JwtDTO jwt = naverService.loginOrSignUp(userInfo);

        return CustomResponse.onSuccess(jwt);
    }
}