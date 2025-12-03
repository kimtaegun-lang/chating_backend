package com.chating.dto.chat;

import java.time.LocalDateTime;

import com.chating.entity.chat.Type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResDTO {
    private Long chatId;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime createdAt;
    private Type type;  
    private String url;
    private String fileName;
    private Long fileSize;
    private Boolean isRead;
}