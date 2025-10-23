package com.chating.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessageDTO {
	@NotNull(message = "채팅방 번호가 필요합니다.")
	private Long roomId;
	
	@NotNull(message = "채팅 번호가 필요합니다.")
	private Long chatId;
}
