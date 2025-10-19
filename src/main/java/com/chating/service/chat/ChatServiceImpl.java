package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.chating.dto.ChatMessageDTO;
import com.chating.entity.member.Chat;
import com.chating.repository.chat.ChatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
	private final ModelMapper modelMapper;
	private final ChatRepository chatRepository;
	public Chat saveMessage(ChatMessageDTO message) {
		Chat chat=modelMapper.map(message, Chat.class);
		chat.setCreatedAt(LocalDateTime.now());
		return chatRepository.save(chat);
	}
	
	public List<Chat>getConversation(String user1,String user2)
	{
		return chatRepository.getConversation(user1, user2);
	}

}
