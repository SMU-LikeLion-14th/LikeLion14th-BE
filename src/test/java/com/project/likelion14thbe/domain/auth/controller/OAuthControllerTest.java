package com.project.likelion14thbe.domain.auth.controller;

import com.project.likelion14thbe.domain.auth.dto.response.JwtDTO;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.auth.service.command.OAuthCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OAuthControllerTest {

    private OAuthCommandService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(OAuthCommandService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OAuthController(service)).build();
    }

    @Test
    void 카카오_콜백은_일반로그인과_동형의_CustomResponse_JwtDTO를_반환한다() throws Exception {
        when(service.handleCallback(eq(Provider.KAKAO), eq("c"), eq("s"), isNull(), any()))
                .thenReturn(JwtDTO.builder().accessToken("A").refreshToken("R").build());

        mockMvc.perform(get("/api/v1/kakao/callback").param("code", "c").param("state", "s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result.accessToken").value("A"))
                .andExpect(jsonPath("$.result.refreshToken").value("R"));
    }

    @Test
    void 네이버_콜백도_동일_형태를_반환한다() throws Exception {
        when(service.handleCallback(eq(Provider.NAVER), eq("c"), eq("s"), isNull(), any()))
                .thenReturn(JwtDTO.builder().accessToken("A").refreshToken("R").build());

        mockMvc.perform(get("/api/v1/naver/callback").param("code", "c").param("state", "s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.result.accessToken").value("A"))
                .andExpect(jsonPath("$.result.refreshToken").value("R"));
    }
}
