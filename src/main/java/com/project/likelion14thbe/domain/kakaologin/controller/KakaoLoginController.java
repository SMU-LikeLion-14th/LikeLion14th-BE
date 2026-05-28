package com.project.likelion14thbe.domain.kakaologin.controller;

import com.project.likelion14thbe.domain.kakaologin.dto.KakaoUserInfoResponseDTO;
import com.project.likelion14thbe.domain.kakaologin.service.KakaoService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
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
@RestController
@RequiredArgsConstructor
@RequestMapping
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("api/v1/auth/kakao")
    public CustomResponse<String> redirectToKakao(
            HttpServletResponse response
    ) throws IOException {

        kakaoService.redirectToKakao(response);
        return CustomResponse.onSuccess(HttpStatus.FOUND, "kakao redirect 완료");
    }

    @GetMapping("api/v1/kakao/callback")
    public CustomResponse<JwtDTO> callback(
            @RequestParam("code") String code
    ) {
        // 1. 카카오 인증서버에서 토큰을 발급받는다.
        // 인가code와 Redirect URL을 파라미터로 전달하여 카카오 인증서버에 요청.
        String accessToken = kakaoService.getAccessTokenFromKakao(code);


        // 2. 1번에서 받은 토큰으로 카카오 리소스 서버에 사용자 정보 요청.
        KakaoUserInfoResponseDTO userInfo = kakaoService.getUserInfo(accessToken);

        //TODO: 3. 회원가입 & 로그인 처리
        // 여기에 서버 사용자 로그인(인증) 또는 회원가입 로직 추가
        // case1: userInfo가 DB에 있으면 로그인
        // case2: userInfo가 DB에 없으면 회원 저장 후 로그인
        //Access토큰, refresh토큰
        JwtDTO jwtTokens = kakaoService.loginOrRegister(userInfo);

        return CustomResponse.onSuccess(jwtTokens);
    }
}