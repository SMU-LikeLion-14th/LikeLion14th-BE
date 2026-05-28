package com.project.likelion14thbe.domain.kakaologin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// DTO에 정의되어 있지 않은 값을 받으면 무시하겠다는 애노테이션
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenResponseDTO(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("refresh_token_expires_in")
        Integer refreshTokenExpiresIn,

        @JsonProperty("scope")
        String scope
) {
}
