package com.chating.config;

public class SecurityConstants {
	// 필터 및 security config에서 허용할 경로 배열
    public static final String[] PUBLIC_PATHS = {
        "/member/signUp",
        "/member/signIn",
        "/api/refresh",
        "/h2-console/**",
        "/ws-chat/**"
    };
}