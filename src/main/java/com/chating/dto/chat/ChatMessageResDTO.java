package com.chating.dto.chat;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageResDTO { // 메시지 전송 res dto
	 private String sender; // 송신자
	    private String receiver; // 수신자
	    private String content; // 내용
	    private LocalDateTime createdAt; // 생성 날짜
	    private Long roomId; // 방 번호
}
