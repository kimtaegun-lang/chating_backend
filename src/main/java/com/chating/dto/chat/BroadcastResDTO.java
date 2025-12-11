package com.chating.dto.chat;

import java.time.LocalDateTime;

import com.chating.entity.chat.State;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BroadcastResDTO {
    private Long chatId;
    private Long chatRoomId;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime createdAt;
    private String type; 
    private String url;
    private String fileName;
    private Long fileSize;
    private Boolean isRead;
    private State state;
}
