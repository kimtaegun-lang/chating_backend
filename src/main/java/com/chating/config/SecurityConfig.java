package com.chating.config;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chating.security.JwtAuthenticationErrorHandler;
import com.chating.util.JwtAuthenticationFilter;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationErrorHandler jwtAuthenticationErrorHandler;
  
    @Value("${FRONTEND_URL}")
    private String frontUrl;
   
    
    // CORS 설정
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        System.out.println("FRONTEND_URL: " + frontUrl);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000","http://localhost:3001","http://localhost:3002")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
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
       return http .cors(cors -> {})
        	// csrf 기능 disabled
            .csrf(csrf -> csrf.disable())
            // 서버가 셰션을 저장하지 않음
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
