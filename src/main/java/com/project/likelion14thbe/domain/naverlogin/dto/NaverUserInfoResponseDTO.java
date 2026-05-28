package com.project.likelion14thbe.domain.naverlogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponseDTO {

    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String id;          // 네이버 회원 고유 식별자
        private String email;
        private String name;
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
        private String gender;
        private String age;
        private String birthday;
        private String mobile;
    }
}
