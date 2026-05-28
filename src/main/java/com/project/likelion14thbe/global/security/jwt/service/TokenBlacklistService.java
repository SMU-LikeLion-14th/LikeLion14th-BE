package com.project.likelion14thbe.global.security.jwt.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // [토큰] → [만료시각 epoch millis]
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /** 블랙리스트 등록 */
    public void addToBlacklist(String accessToken, long expirationMillis) {
        if (expirationMillis <= 0) return;
        blacklist.put(accessToken, System.currentTimeMillis() + expirationMillis);
    }

    /** 블랙리스트 여부 확인 (만료된 항목은 자동 정리) */
    public boolean isBlacklisted(String accessToken) {
        Long expireAt = blacklist.get(accessToken);
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            blacklist.remove(accessToken);
            return false;
        }
        return true;
    }
}
