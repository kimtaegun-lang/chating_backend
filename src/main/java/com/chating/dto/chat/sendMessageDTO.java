package com.chating.dto.chat;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class sendMessageDTO { // 메시지 전송 req dto
    
    private String sender;      // 보낸 사람
    
    @NotBlank(message = "수신자 정보가 필요합니다")
    private String receiver;     // 받는 사람 
       
    @NotNull(message = "채팅방 번호가 필요합니다")
    private Long roomId; // 방 번호

    private String content;     // 메시지 내용
    
    private MultipartFile file; // 이미지 및 파일
    
    
}