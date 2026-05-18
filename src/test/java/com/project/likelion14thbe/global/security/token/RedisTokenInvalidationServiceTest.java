package com.project.likelion14thbe.global.security.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisTokenInvalidationServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private RedisTokenInvalidationService service;

    private static final long REFRESH_TTL_MS = 1_209_600_000L;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new RedisTokenInvalidationService(redisTemplate, REFRESH_TTL_MS);
    }

    @Test
    void invalidateUser_storesCutoffWithRefreshTtl() {
        service.invalidateUser("kim@test.com");

        verify(valueOps).set(
                eq("auth:invalidate-before:kim@test.com"),
                anyString(),
                eq(Duration.ofMillis(REFRESH_TTL_MS))
        );
    }

    @Test
    void isInvalidated_returnsFalse_whenNoCutoffKey() {
        when(valueOps.get("auth:invalidate-before:kim@test.com")).thenReturn(null);

        assertThat(service.isInvalidated("kim@test.com", System.currentTimeMillis())).isFalse();
    }

    @Test
    void isInvalidated_returnsTrue_whenTokenIssuedStrictlyBeforeCutoff() {
        when(valueOps.get("auth:invalidate-before:kim@test.com")).thenReturn("2000");

        assertThat(service.isInvalidated("kim@test.com", 1999L)).isTrue();
    }

    @Test
    void isInvalidated_returnsFalse_whenTokenIssuedAtCutoff_strictBoundary() {
        when(valueOps.get("auth:invalidate-before:kim@test.com")).thenReturn("2000");

        assertThat(service.isInvalidated("kim@test.com", 2000L)).isFalse();
    }

    @Test
    void isInvalidated_failsOpen_whenRedisThrows() {
        when(valueOps.get(anyString())).thenThrow(new QueryTimeoutException("redis down"));

        assertThat(service.isInvalidated("kim@test.com", 1L)).isFalse();
    }

    @Test
    void isInvalidated_failsOpen_whenCutoffValueIsCorrupt() {
        when(valueOps.get("auth:invalidate-before:kim@test.com")).thenReturn("not-a-number");

        assertThat(service.isInvalidated("kim@test.com", 1L)).isFalse();
    }

    @Test
    void invalidateUser_doesNotRethrow_whenRedisThrows() {
        org.mockito.Mockito.doThrow(new org.springframework.dao.QueryTimeoutException("redis down"))
                .when(valueOps).set(anyString(), anyString(), any(java.time.Duration.class));

        // fail-open: must return normally, not propagate
        service.invalidateUser("kim@test.com");
    }
}
