package com.project.likelion14thbe.global.config;

import com.project.likelion14thbe.global.security.exception.JwtAccessDeniedHandler;
import com.project.likelion14thbe.global.security.exception.JwtAuthenticationEntryPoint;
import com.project.likelion14thbe.global.security.filter.CustomLoginFilter;
import com.project.likelion14thbe.global.security.filter.JwtAuthorizationFilter;
import com.project.likelion14thbe.global.security.handler.CustomLogoutHandler;
import com.project.likelion14thbe.global.security.handler.CustomLogoutSuccessHandler;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.service.TokenBlacklistService; // ✅ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@EnableMethodSecurity // @PreAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final TokenBlacklistService tokenBlacklistService;

    private final String[] allowUrl = {
            "/api/v1/members/signup",
            "/api/v1/members/login",
            "/api/v1/members/logout",
            "/api/v1/auth/kakao",
            "/api/v1/kakao/callback",
            "/api/v1/auth/naver/**",
            "/auth/**",
            "/api/usage",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CustomLoginFilter loginFilter = new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/v1/members/login");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(allowUrl).permitAll()
                        .anyRequest().authenticated()
                )
                // 필터 생성자에 tokenBlacklistService 전달
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")  // ✅ AuthController 매핑에 맞게 변경
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
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