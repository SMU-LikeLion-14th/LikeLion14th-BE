package com.project.likelion14thbe.domain.kakaologin.service;

import com.project.likelion14thbe.domain.auth.exception.AuthErrorCode;
import com.project.likelion14thbe.domain.auth.exception.AuthException;
import com.project.likelion14thbe.domain.kakaologin.dto.KakaoTokenResponseDTO;
import com.project.likelion14thbe.domain.kakaologin.dto.KakaoUserInfoResponseDTO;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class KakaoService {

    // 이 값은 항상 고정이므로 환경변수화 하지 않고, yml 파일에 저장하지 않음
    private static final String authorizationURI = "https://kauth.kakao.com/oauth/authorize";
    private static final String tokenURI = "https://kauth.kakao.com/oauth/token";
    private static final String userInfoURI = "https://kapi.kakao.com/v2/user/me";

    // 실행 환경 마다 변경되는 값 또는 민감한 값이므로 yml에 환경 변수로 저장
    private final String clientId;
    private final String clientSecret;
    private final String redirectURI;

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 생성자 주입 방식을 사용하는 이유는 clientId, clientSecret, redirectURI를 불변(final)하게 하기 위해서는 생성자 주입을 해야 함
    public KakaoService(
            @Value("${spring.kakao.client.id}") String clientId,
            @Value("${spring.kakao.client.secret}") String clientSecret,
            @Value("${spring.kakao.uri.redirect}") String redirectURI,
            MemberRepository memberRepository,
            JwtUtil jwtUtil,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectURI = redirectURI;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public void redirectToKakao(HttpServletResponse response) throws IOException {

        // https://kauth.kakao.com/oauth/authorize
        // ?response_type=code
        // &client_id=${REST_API_KEY}
        // &redirect_uri=${REDIRECT_URI}
        // 를 만드는 코드
        String redirectUrl = UriComponentsBuilder
                .fromUriString(authorizationURI)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectURI)
                .build()
                .toUriString();

        // code 발급을 위한 redirect
        response.sendRedirect(redirectUrl);
    }

    public String getAccessTokenFromKakao(String code) {

        // 실제 서비스에서는 절대 사용해서는 안됨(값 노출) -> 로컬 개발 시 디버깅 용도에만 사용
        log.info("[KakaoService] tokenURI={}", tokenURI);
        log.info("[KakaoService] redirectURI={}", redirectURI);
        log.info("[KakaoService] clientId={}", clientId);
        log.info("[KakaoService] code={}", code);

        KakaoTokenResponseDTO kakaoTokenResponseDto = null;
        try {
            // WebClient 요청으로 카카오에서 액세스 토큰을 받아오기
            kakaoTokenResponseDto = WebClient.create()
                    // post 요청
                    .post()
                    // tokenURI로
                    .uri(tokenURI)
                    // 요청 헤더의 Content-Type을 application/x-www-form-urlencoded로 설정
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    // requestBody에 담을 값들
                    .body(
                            BodyInserters.fromFormData("grant_type", "authorization_code")
                                    .with("client_id", clientId)
                                    .with("client_secret", clientSecret)
                                    .with("redirect_uri", redirectURI)
                                    .with("code", code)
                    )
                    .retrieve()
                    // 카카오 서버가 보내준 정보를 KakaoTokenResponseDTO 형태로 역직렬화 해서 받겠다
                    // 그래서 반환 값을 아는 것이 중요하다
                    .bodyToMono(KakaoTokenResponseDTO.class)
                    // 동기 방식
                    .block();
            // 예외 처리
        } catch (WebClientRequestException e) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_OAUTH_REQUEST);
        }
        // kakaoTokenResponseDto가 null이거나, kakaoTokenResponseDto의 accessToken이 null일 때
        if (kakaoTokenResponseDto == null || kakaoTokenResponseDto.accessToken() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }

        // 실제 서비스에서는 절대 사용해서는 안됨(값 노출) -> 로컬 개발 시 디버깅 용도에만 사용
        log.info("[Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.accessToken());
        log.info("[Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.refreshToken());
        log.info("[Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.idToken());
        log.info("[Kakao Service] Scope ------> {}", kakaoTokenResponseDto.scope());

        // access token을 반환
        return kakaoTokenResponseDto.accessToken();
    }

    public KakaoUserInfoResponseDTO getUserInfo(String accessToken) {

        KakaoUserInfoResponseDTO userInfo = null;
        try {
            // 카카오에서 받은 access token으로 카카오 유저 프로필 조회
            userInfo = WebClient.create()
                    // 조회이므로 get 요청
                    .get()
                    // userInfoURI로
                    .uri(userInfoURI)
                    // request header에 카카오에서 받은 access token을 포함
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    // 카카오 서버가 보내준 정보를 KakaoUserInfoResponseDTO 형태로 역직렬화 해서 받겠다
                    // 그래서 반환 값을 아는 것이 중요하다
                    .bodyToMono(KakaoUserInfoResponseDTO.class)
                    // 동기 방식
                    .block();
        } catch (WebClientRequestException e) {
            throw new AuthException(AuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_OAUTH_REQUEST);
        }

        // KakaoUserInfoResponseDTO가 null인 경우
        if (userInfo == null) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }

        log.info("[Kakao Service] userInfo ------> {}", userInfo);

        // 실제 서비스에서는 절대 사용해서는 안됨(값 노출) -> 로컬 개발 시 디버깅 용도에만 사용
        if (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null) {
            log.info("[Kakao Service] Auth ID ---> {}", userInfo.id());
            log.info("[Kakao Service] NickName ---> {}", userInfo.kakaoAccount().profile().nickName());
            log.info("[Kakao Service] ProfileImageUrl ---> {}", userInfo.kakaoAccount().profile().profileImageUrl());
        }

        return userInfo;
    }

    public JwtDTO loginOrRegister(KakaoUserInfoResponseDTO userInfo) {
        String email = userInfo.kakaoAccount().email();
        String nickname = (userInfo.kakaoAccount().profile() != null)
                ? userInfo.kakaoAccount().profile().nickName()
                : "kakao_" + userInfo.id();

        // 데이터베이스에 회원이 있으면 그 회원으로 로그인하고 없으면 회원가입 후 로그인
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("[ KakaoService ] 신규 카카오 회원 가입 진행: {}", email);

                    String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

                    Member newMember = Member.builder()
                            .email(email)
                            .name(nickname)
                            .password(randomPassword)
                            .age(0)
                            .role(Role.ROLE_USER)
                            .build();

                    return memberRepository.save(newMember);
                });

        log.info("[ KakaoService ] 카카오 로그인 완료. memberId = {}", member.getId());

        CustomUserDetails userDetails = new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );

        String accsssToken = jwtUtil.createJwtAccessToken(userDetails);
        String refreshToken = jwtUtil.createJwtRefreshToken(userDetails);

        return JwtDTO.builder()
                .accessToken(accsssToken)
                .refreshToken(refreshToken)
                .build();
    }
}