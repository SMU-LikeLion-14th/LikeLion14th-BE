package com.project.likelion14thbe.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {

        log.info("[ CustomLogoutSuccessHandler ] 로그아웃 성공 응답을 반환합니다.");

        CustomResponse<String> body = CustomResponse.onSuccess("로그아웃이 완료되었습니다.");

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}
