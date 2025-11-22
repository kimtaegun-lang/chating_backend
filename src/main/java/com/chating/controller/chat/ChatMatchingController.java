package com.chating.controller.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.service.chat.ChatMatchingService;
import com.chating.util.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatMatchingController {
    private final JwtUtil jwtUtil;
    private final ChatMatchingService chatMatchingService;
    
    // 랜덤 매칭
    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/random/match")
    public void requestMatch(SimpMessageHeaderAccessor headerAccessor) {
        String userId =(String) headerAccessor.getSessionAttributes().get("userId");
        String role = (String) headerAccessor.getSessionAttributes().get("role");
        chatMatchingService.randomMatching(userId,role);
        
    }
    
    // 매칭 취소
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/random/cancel")
    public ResponseEntity<String> cancelMatch() {
    	String userId =jwtUtil.getLoginId();
    	String role=jwtUtil.getRole();
        chatMatchingService.cancelMatching(userId,role);
        return ResponseEntity.ok("매칭 취소 완료");
    }
}