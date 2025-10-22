package com.chating.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO { // 메시지 전송 req dto
    
    private String sender;      // 보낸 사람
    
    @NotBlank(message = "수신자 정보가 필요합니다")
    private String receiver;     // 받는 사람 
    
    @NotBlank(message = "메시지 내용이 필요합니다")
    private String content;     // 메시지 내용
    
    @NotNull(message = "채팅방 번호가 필요합니다")
    private Long roomId; // 방 번호
}