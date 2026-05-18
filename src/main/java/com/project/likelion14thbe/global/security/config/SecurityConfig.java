package com.project.likelion14thbe.global.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.likelion14thbe.global.apiPayload.CustomResponse;
import com.project.likelion14thbe.global.security.filter.CustomLoginFilter;
import com.project.likelion14thbe.global.security.filter.JwtAuthorizationFilter;
import com.project.likelion14thbe.global.security.handler.CustomLogoutHandler;
import com.project.likelion14thbe.global.security.handler.JwtAccessDeniedHandler;
import com.project.likelion14thbe.global.security.handler.JwtAuthenticationEntryPoint;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomLogoutHandler customLogoutHandler;
    private final TokenInvalidationService tokenInvalidationService;

    // 인증 없이 접근 허용할 URL
    private final String[] allowUrl = {
            "/api/v1/login",                 // CustomLoginFilter 처리
            "/api/v1/auth/kakao",            // 카카오 인가 진입
            "/api/v1/kakao/callback",        // 카카오 콜백
            "/api/v1/auth/naver",            // 네이버 인가 진입
            "/api/v1/naver/callback",        // 네이버 콜백
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CustomLoginFilter loginFilter =
                new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/v1/login");

        http
                .cors(cors -> cors.configurationSource(CorsConfig.apiConfigurationSource()))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(allowUrl).permitAll()
                        // 회원가입 (POST /api/v1/members) — 인증 없이 허용
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        // 그 외는 인증 필수
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil, tokenInvalidationService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setCharacterEncoding("UTF-8");
                            res.getWriter().write(
                                    new ObjectMapper().writeValueAsString(
                                            CustomResponse.onSuccess("로그아웃 성공")
                                    )
                            );
                        }))
                .exceptionHandling(eh -> eh
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
