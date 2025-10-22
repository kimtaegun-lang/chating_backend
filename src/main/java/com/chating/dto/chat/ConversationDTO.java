package com.chating.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO { // 채팅 내역 req dto
    
    @NotBlank(message = "사용자1 정보가 필요합니다")
    private String user1; // 회원 아이디1
    
    @NotBlank(message = "사용자2 정보가 필요합니다")
    private String user2; // 회원 아이디2 
    
    @NotNull(message = "로드할 메시지 개수는 양수여야 합니다")
    private int limit; // 한번에 로드한 메시지 갯수
    
    @NotNull(message = "채팅 ID는 양수여야 합니다")
    private int chatId; // 채팅 id
    
    @NotNull(message="방 번호 ID가 존재하지 않습니다.")
    private int roomId;
}