package com.project.likelion14thbe.global.security.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RedisTokenInvalidationService implements TokenInvalidationService {

    private static final String KEY_PREFIX = "auth:invalidate-before:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpMs;

    public RedisTokenInvalidationService(
            StringRedisTemplate redisTemplate,
            @Value("${spring.jwt.token.refresh-expiration-time:1209600000}") long refreshExpMs
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshExpMs = refreshExpMs;
    }

    /**
     * 컷오프를 millis로 기록한다. 단, JWT iat는 RFC 7519상 초 단위라
     * getIssuedAt()은 항상 1000ms 배수다. 따라서 로그아웃과 같은 wall-clock
     * 초 안에 재로그인하면, 새 토큰 iat(= 그 초의 millis)가 cutoff보다 작아
     * 최대 약 1초 동안 정상 토큰이 401로 거부될 수 있다. 이는 의도된 수용
     * 한계다: 보안상 안전한 방향(과다 무효화)으로 실패하며, 로그아웃된
     * 토큰이 살아남지 않는다. 완전 해소는 per-token jti(후속 S3 범위)로만
     * 가능하다.
     */
    @Override
    public void invalidateUser(String email) {
        String key = KEY_PREFIX + email;
        long now = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue()
                    .set(key, String.valueOf(now), Duration.ofMillis(refreshExpMs));
            log.info("[ TokenInvalidation ] 컷오프 기록 : {} = {}", email, now);
        } catch (DataAccessException e) {
            // 후속 관측성 phase: Redis 불가 시 메트릭 카운터 hook
            log.warn("[ TokenInvalidation ] 컷오프 기록 실패(fail-open) : {} : {}", email, e.getMessage());
        }
    }

    @Override
    public boolean isInvalidated(String email, long tokenIatMillis) {
        String key = KEY_PREFIX + email;
        try {
            String cutoffRaw = redisTemplate.opsForValue().get(key);
            if (cutoffRaw == null) {
                return false;
            }
            long cutoff = Long.parseLong(cutoffRaw);
            return tokenIatMillis < cutoff;
        } catch (DataAccessException e) {
            // 후속 관측성 phase: Redis 불가 시 메트릭 카운터 hook
            log.warn("[ TokenInvalidation ] 컷오프 조회 실패(fail-open) : {} : {}", email, e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            log.warn("[ TokenInvalidation ] 컷오프 값 파싱 실패(fail-open) : {} : {}", email, e.getMessage());
            return false;
        }
    }
}
