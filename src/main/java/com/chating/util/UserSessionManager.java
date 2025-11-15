package com.chating.util;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionManager {

    private final StringRedisTemplate redisTemplate;
    private HashOperations<String, String, String> hashOps;
    
    private static final String USER_TO_SESSION = "ws:userToSession";   // userId → sessionId
    private static final String SESSION_TO_USER = "ws:sessionToUser";   // sessionId → userId
    private static final String USER_TO_SERVER = "ws:userToServer";     // userId → serverId
    
    private final String serverId = System.getenv().getOrDefault("SERVER_ID", "LOCAL");

    @PostConstruct
    public void init() {
        hashOps = redisTemplate.opsForHash();
    }

    // WebSocket 연결 시 userId ↔ sessionId ↔ serverId 매핑 저장
    public void register(String userId, String sessionId) {
        hashOps.put(USER_TO_SESSION, userId, sessionId);
        hashOps.put(SESSION_TO_USER, sessionId, userId);
        hashOps.put(USER_TO_SERVER, userId, serverId);
    }

    // WebSocket 연결 해제 시 매핑 제거
    public void unregister(String sessionId) {
        String userId = hashOps.get(SESSION_TO_USER, sessionId);
        if (userId != null) {
            hashOps.delete(SESSION_TO_USER, sessionId);
            
            // 저장된 sessionId가 현재 sessionId와 같을 때만 삭제 (중복 연결 방지)
            String storedSessionId = hashOps.get(USER_TO_SESSION, userId);
            if (sessionId.equals(storedSessionId)) {
                hashOps.delete(USER_TO_SESSION, userId);
                hashOps.delete(USER_TO_SERVER, userId);
            }
        }
    }

    // 특정 유저가 이 서버에 연결되어 있는지 확인
    public boolean isUserOnThisServer(String userId) {
        String storedServerId = hashOps.get(USER_TO_SERVER, userId);
        return serverId.equals(storedServerId);
    }

    // userId로 WebSocket sessionId 조회
    public String getSessionId(String userId) {
        return hashOps.get(USER_TO_SESSION, userId);
    }
}