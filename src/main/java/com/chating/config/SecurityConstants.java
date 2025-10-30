package com.chating.config;

public class SecurityConstants {
    public static final String[] PUBLIC_PATHS = {
        "/member/signUp",
        "/member/signIn",
        "/api/refresh",
        "/h2-console/**",
        "/ws-chat/**"
    };
}