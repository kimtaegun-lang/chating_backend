package com.chating.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.chating.security.JwtAuthenticationErrorHandler;
import com.chating.util.JwtAuthenticationFilter;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationErrorHandler jwtAuthenticationErrorHandler;

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String frontUrl = System.getenv("FRONTEND_URL");
        
        System.out.println("====================================");
        System.out.println("🌐 FRONTEND_URL: " + frontUrl);
        System.out.println("====================================");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 환경변수 있으면 사용, 없으면 모든 출처 허용
        if (frontUrl != null && !frontUrl.isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(frontUrl));
        } else {
            configuration.addAllowedOriginPattern("*");
            System.out.println("⚠️ FRONTEND_URL 없음, 모든 출처 허용");
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    // 비밀번호 암호화: BCrypt 단방향 암호화(복호화 불가능)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 로그인 시 비밀번호 아이디 검증 매니저
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // HTTP 요청에만 사용됨 Spring Security 보안 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // csrf 기능 disabled
            .csrf(csrf -> csrf.disable())
            // 서버가 세션을 저장하지 않음
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 토큰 없이 접근 가능한 경로 설정
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/member/signUp").permitAll()
                .requestMatchers("/member/signIn").permitAll()
                .requestMatchers("/api/refresh").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/ws-chat/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationErrorHandler)
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, jwtAuthenticationErrorHandler),
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }
}