package com.project.likelion14thbe.global.security.token;

/**
 * 사용자별 토큰 무효화 컷오프를 기록·조회한다.
 * 로그아웃·비밀번호 변경·탈퇴가 공통으로 사용한다.
 */
public interface TokenInvalidationService {

    /**
     * 지금 이후로 {email}이 그 전에 발급받은 토큰을 모두 무효화한다.
     */
    void invalidateUser(String email);

    /**
     * 토큰 발급 시각이 컷오프보다 엄격히 이전이면 무효 처리한다.
     *
     * @param tokenIatMillis JWT iat (epoch millis)
     * @return 무효면 true. 키 없음 또는 Redis 장애 시 false(fail-open).
     */
    boolean isInvalidated(String email, long tokenIatMillis);
}
