package com.chating.dto.chat;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResDTO { // 채팅방 res dto
    private Long roomId; // 방 번호
    private String receiver;  // 상대방 ID
    private LocalDateTime createdAt; // 방 생성 일자
}