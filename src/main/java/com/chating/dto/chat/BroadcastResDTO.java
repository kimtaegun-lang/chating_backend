package com.chating.dto.chat;

import java.time.LocalDateTime;
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
    private String type;  // "CREATE" or "DELETE"
    
    // 파일 관련 필드 추가 (없으면 추가하세요!)
    private String url;
    private String fileName;
    private Long fileSize;
}
