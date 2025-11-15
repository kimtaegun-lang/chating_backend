package com.chating.util;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chating.dto.chat.BroadcastResDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 서버별 큐를 구독하는 단일 리스너
     * 모든 채팅방 메시지를 처리
     */
    @RabbitListener(
        queues = "#{serverChatQueue.name}",
        messageConverter = "jackson2JsonMessageConverter"
    )
    public void handleChatMessage(BroadcastResDTO message) {
        try {
            Long roomId = message.getChatRoomId();
            
            if (roomId == null) {
                log.warn("⚠️ roomId가 없는 메시지: {}", message);
                return;
            }
            
            // 해당 채팅방을 구독 중인 모든 클라이언트에게 STOMP 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chatroom-" + roomId, message);
            
            log.info("✅ 채팅 메시지 브로드캐스트: roomId={}, type={}, chatId={}", 
                roomId, message.getType(), message.getChatId());
                
        } catch (Exception e) {
            log.error("❌ 채팅 메시지 처리 실패", e);
        }
    }
}