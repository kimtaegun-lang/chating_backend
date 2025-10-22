// RandomChatController.java
package com.chating.controller.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.service.chat.ChatMatchingService;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatMatchingController {
    private final JwtUtil jwtUtil;
    private final ChatMatchingService chatMatchingService;
    
    // 랜덤 매칭
    @MessageMapping("/random/match")
    public void requestMatch(SimpMessageHeaderAccessor headerAccessor) {
        String userId =(String) headerAccessor.getSessionAttributes().get("userId");
        chatMatchingService.randomMatching(userId);
    }
    
    // 매칭 취소
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/random/cancel")
    public ResponseEntity<String> cancelMatch() {
    	String userId =jwtUtil.getLoginId();
        chatMatchingService.cancelMatching(userId);
        return ResponseEntity.ok("매칭 취소 완료");
    }
}