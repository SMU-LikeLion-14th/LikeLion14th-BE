package com.project.likelion14thbe.domain.naverlogin.service;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.domain.naverlogin.dto.NaverTokenResponseDTO;
import com.project.likelion14thbe.domain.naverlogin.dto.NaverUserInfoResponseDTO;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @Value("${naver.auth-uri}")
    private String authUri;

    @Value("${naver.token-uri}")
    private String tokenUri;

    @Value("${naver.user-info-uri}")
    private String userInfoUri;

    /**
     * 네이버 로그인 인증 URL 생성
     */
    public String getAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        return authUri
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    /**
     * authorization code -> access token
     */
    public NaverTokenResponseDTO getAccessToken(String code, String state) {
        log.info("[NaverService] tokenUri={}", tokenUri);
        log.info("[NaverService] clientId={}", clientId);
        log.info("[NaverService] code={}", code);
        log.info("[NaverService] state={}", state);

        try {
            NaverTokenResponseDTO tokenResponse = WebClient.create()
                    .post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("code", code)
                            .with("state", state))
                    .retrieve()
                    .bodyToMono(NaverTokenResponseDTO.class)
                    .block();

            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.error("[NaverService] 토큰 응답이 비어있거나 access_token 없음. error={}, desc={}",
                        tokenResponse != null ? tokenResponse.getError() : null,
                        tokenResponse != null ? tokenResponse.getErrorDescription() : null);
                throw new RuntimeException("네이버 access_token 발급 실패");
            }

            log.info("[NaverService] access_token 발급 성공");
            return tokenResponse;

        } catch (WebClientResponseException e) {
            log.error("[NaverService] 네이버 토큰 요청 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("네이버 토큰 요청 실패", e);
        }
    }

    /**
     * access token -> 사용자 정보 조회
     */
    public NaverUserInfoResponseDTO getUserInfo(String accessToken) {
        try {
            NaverUserInfoResponseDTO userInfo = WebClient.create()
                    .get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(NaverUserInfoResponseDTO.class)
                    .block();

            log.info("[NaverService] 사용자 정보 조회 성공: {}", userInfo);
            return userInfo;

        } catch (WebClientResponseException e) {
            log.error("[NaverService] 사용자 정보 조회 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("네이버 사용자 정보 조회 실패", e);
        }
    }

    /**
     * 네이버 사용자 정보로 로그인/회원가입 처리 후 우리 서버의 JWT 발급
     */
    @Transactional
    public JwtDTO loginOrSignUp(NaverUserInfoResponseDTO userInfo) {
        if (userInfo == null || userInfo.getResponse() == null) {
            throw new RuntimeException("네이버 사용자 정보 조회 실패");
        }
        NaverUserInfoResponseDTO.Response info = userInfo.getResponse();
        String email = info.getEmail();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("네이버 계정에서 이메일을 가져올 수 없습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("[NaverService] 신규 네이버 회원 가입: {}", email);
                    return memberRepository.save(
                            Member.builder()
                                    .email(email)
                                    .name(info.getName() != null ? info.getName() : info.getNickname())
                                    .photoImg(info.getProfileImage())
                                    .age(parseAge(info.getAge()))
                                    .password(passwordEncoder.encode("NAVER_" + UUID.randomUUID()))
                                    .role(Role.ROLE_USER)
                                    .build()
                    );
                });

        log.info("[NaverService] 네이버 로그인 완료. memberId={}", member.getId());

        CustomUserDetails userDetails = new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );

        return JwtDTO.builder()
                .accessToken(jwtUtil.createJwtAccessToken(userDetails))
                .refreshToken(jwtUtil.createJwtRefreshToken(userDetails))
                .build();
    }

    /**
     * 네이버에서 내려주는 age 값("20-29")을 정수로 변환 (시작 나이대로 변환).
     * 형식이 다르거나 null 이면 null 반환.
     */
    private Integer parseAge(String age) {
        if (age == null || age.isBlank()) {
            return null;
        }
        try {
            if (age.contains("-")) {
                return Integer.parseInt(age.split("-")[0].trim());
            }
            return Integer.parseInt(age.trim());
        } catch (NumberFormatException e) {
            log.warn("네이버 age 파싱 실패: {}", age);
            return null;
        }
    }
}
