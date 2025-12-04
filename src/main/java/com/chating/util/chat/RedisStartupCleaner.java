package com.chating.util.chat;

import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisStartupCleaner {
	// 서버 재가동 시 redis 초기화

    private final StringRedisTemplate redisTemplate;

    private static final String USER_TO_SESSION = "ws:userToSession";
    private static final String SESSION_TO_USER = "ws:sessionToUser";
    private static final String USER_TO_SERVER = "ws:userToServer";

    @PostConstruct
    public void clearOnStartup() {

        redisTemplate.delete(USER_TO_SESSION);
        redisTemplate.delete(SESSION_TO_USER);
        redisTemplate.delete(USER_TO_SERVER);

    }
}
