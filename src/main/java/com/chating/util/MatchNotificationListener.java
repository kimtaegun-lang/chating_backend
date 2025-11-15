package com.chating.util;

import java.util.Map;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchNotificationListener implements MessageListener {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final UserSessionManager sessionManager;

    // Redis "match:notifications" 채널 구독 등록
    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(
            this, 
            new ChannelTopic("match:notifications")
        );
    }

    // Redis Pub/Sub 메시지 수신 시 WebSocket으로 클라이언트에 전달
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            Map<String, Object> payload = objectMapper.readValue(body, Map.class);
            
            String targetUserId = (String) payload.get("userId");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            
            if (targetUserId == null || data == null) {
                return;
            }
            
            // 이 서버에 해당 유저가 연결되어 있는지 확인
            if (!sessionManager.isUserOnThisServer(targetUserId)) {
                return;
            }
            
            // 유저의 WebSocket sessionId 가져오기
            String sessionId = sessionManager.getSessionId(targetUserId);
            if (sessionId == null) {
                return;
            }
            
            // SimpleBroker가 생성한 실제 구독 경로로 메시지 전송
            String destination = "/queue/match-user" + sessionId;
            messagingTemplate.convertAndSend(destination, data);
            
                    
        } catch (Exception e) {
        }
    }
}