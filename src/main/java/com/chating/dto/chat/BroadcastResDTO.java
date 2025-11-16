package com.chating.dto.chat;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class BroadcastResDTO { // 메시지 전송 res dto
	//private static final long serialVersionUID = 1L;
		private String sender; // 송신자
	    private String receiver; // 수신자
	    private String content; // 내용
	    private LocalDateTime createdAt; // 생성 날짜
	    private String type; // 추가, 삭제 타입
	    private Long chatId; // 채팅 아이디
	    private Long chatRoomId; // 채팅방 번호
} */

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
