package com.chating.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO { // 메시지 전송 req dto
    private String sender;      // 보낸 사람
    private String receiver;     // 받는 사람 
    private String content;     // 메시지 내용
    private Long roomId; // 방 번호
}