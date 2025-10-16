package com.chating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String sender;      // 보낸 사람
    private String receiver;     // 받는 사람 
    private String content;     // 메시지 내용
}