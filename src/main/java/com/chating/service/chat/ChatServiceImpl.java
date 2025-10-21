package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chating.dto.chat.ChatMessageDTO;
import com.chating.dto.chat.ChatMessageResDTO;
import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.ConversationResDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.entity.chat.Chat;
import com.chating.entity.chat.ChatRoom;
import com.chating.repository.chat.ChatRepository;
import com.chating.repository.chat.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
	private final ModelMapper modelMapper;
	private final ChatRepository chatRepository;
	private final ChatRoomRepository chatRoomRepository;

	// 채팅방 생성 또는 조회
	@Override
	public ChatRoom createChatRoom(String sender, String receiver) {
		if (sender == null || sender.trim().isEmpty()) {
			throw new IllegalArgumentException("발신자 정보가 필요합니다");
		}
		if (receiver == null || receiver.trim().isEmpty()) {
			throw new IllegalArgumentException("수신자 정보가 필요합니다");
		}
		if (sender.equals(receiver)) {
			throw new IllegalArgumentException("자신과는 채팅할 수 없습니다");
		}

		Optional<ChatRoom> opt = chatRoomRepository.findRoomByIds(sender, receiver);

		if (opt.isPresent()) {
			return opt.get();
		}

		ChatRoom newRoom = ChatRoom.builder()
				.user1(sender)
				.user2(receiver)
				.createdAt(LocalDateTime.now())
				.chat(new ArrayList<>())
				.build();

		
		return chatRoomRepository.save(newRoom);
	}

	// 메시지 저장
	@Override
	public ChatMessageResDTO saveMessage(ChatMessageDTO message) {
		if (message == null) {
			throw new IllegalArgumentException("메시지 정보가 필요합니다");
		}
		if (message.getSender() == null || message.getSender().trim().isEmpty()) {
			throw new IllegalArgumentException("발신자 정보가 필요합니다");
		}
		if (message.getReceiver() == null || message.getReceiver().trim().isEmpty()) {
			throw new IllegalArgumentException("수신자 정보가 필요합니다");
		}
		if (message.getContent() == null || message.getContent().trim().isEmpty()) {
			throw new IllegalArgumentException("메시지 내용이 필요합니다");
		}
		if (message.getRoomId() == null) {
			throw new IllegalArgumentException("채팅방 번호가 필요합니다");
		}

		ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다"));

		Chat chat = new Chat();
		chat.setSender(message.getSender());
		chat.setReceiver(message.getReceiver());
		chat.setContent(message.getContent());
		chat.setCreatedAt(LocalDateTime.now());
		chat.setChatroom(chatRoom);

		Chat savedChat = chatRepository.save(chat);

		return modelMapper.map(savedChat, ChatMessageResDTO.class);
	}

	// 대화 내역 조회
	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<ConversationResDTO> getConversation(ConversationDTO conversationDTO) {

	    if (conversationDTO == null) throw new IllegalArgumentException("대화 조회 정보가 필요합니다");
	    if (conversationDTO.getUser1() == null || conversationDTO.getUser1().trim().isEmpty())
	        throw new IllegalArgumentException("사용자1 정보가 필요합니다");
	    if (conversationDTO.getUser2() == null || conversationDTO.getUser2().trim().isEmpty())
	        throw new IllegalArgumentException("사용자2 정보가 필요합니다");

	    int size = conversationDTO.getLimit() > 0 ? conversationDTO.getLimit() : 10;
	    Long chatId = conversationDTO.getChatId() > 0 ? (long) conversationDTO.getChatId() : Long.MAX_VALUE;

	    Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());

	    Page<ConversationResDTO> dtoPage = chatRepository.getConversationByChatId(
	        conversationDTO.getUser1(),
	        conversationDTO.getUser2(),
	        chatId,
	        pageable
	    );

	    PageResponseDTO<ConversationResDTO> response = new PageResponseDTO<>(dtoPage);

	    if (!dtoPage.getContent().isEmpty()) {
	        int lastIndex = dtoPage.getContent().size() - 1;
	        response.setCurrentPage(dtoPage.getContent().get(lastIndex).getChatId().intValue());
	    } else {
	        response.setCurrentPage(0);
	    }

	    return response;
	}
	  
	  // 본인 채팅방 목록 조회
		@Override
		@Transactional(readOnly = true)
		public List<ChatRoomResDTO> getMyChatRooms(String userId) {
		    if (userId == null || userId.trim().isEmpty()) {
		        throw new IllegalArgumentException("사용자 정보가 필요합니다");
		    }
		    
		    return chatRoomRepository.findMyChatRooms(userId);
		    
		}
}