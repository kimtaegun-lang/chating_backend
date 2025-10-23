package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chating.common.CustomException;
import com.chating.dto.chat.BroadcastResDTO;
import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.ConversationResDTO;
import com.chating.dto.chat.DeleteMessageDTO;
import com.chating.dto.chat.sendMessageDTO;
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
    private final SimpMessagingTemplate messagingTemplate;
    
    // 메시지 저장
    @Override
    public void saveMessage(sendMessageDTO message) {
        
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findRoomByIds(
                message.getSender(), 
                message.getReceiver()
            )
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "유효하지 않는 채팅방 입니다."
            ));
       
        // 메시지 저장
        Chat chat = Chat.builder()
            .sender(message.getSender())
            .receiver(message.getReceiver())
            .content(message.getContent())
            .createdAt(LocalDateTime.now())
            .chatroom(chatRoom)
            .build();

        Chat savedChat = chatRepository.save(chat);
        BroadcastResDTO response =modelMapper.map(savedChat, BroadcastResDTO.class);
        response.setType("CREATE");
        
        messagingTemplate.convertAndSend("/topic/chatroom-" + message.getRoomId(), response);
    }

    // 채팅 삭제
    @Transactional
    public void deleteChat(DeleteMessageDTO message) {
        Chat chat = chatRepository.findById(message.getChatId())
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "해당 채팅을 찾을 수 없습니다."
            ));
        
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(
                message.getRoomId())
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "유효하지 않은 채팅방 입니다."
            ));
        
        chatRepository.deleteById(message.getChatId());
        
        // 삭제 알림 객체 생성
        BroadcastResDTO response=BroadcastResDTO.builder()
            .chatId(message.getChatId())
            .type("DELETE")
            .build();
        
        messagingTemplate.convertAndSend("/topic/chatroom-" + message.getRoomId(), response);
    }
    
    // 대화 내역 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ConversationResDTO> getConversation(ConversationDTO conversationDTO) {
        
    	// 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findRoomByIds(
                conversationDTO.getUser1(), 
                conversationDTO.getUser2()
            )
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "회원들간 채팅방을 찾을 수 없습니다."
            ));
        
        if(chatRoom.getRoomId()!=conversationDTO.getRoomId())
        {
        	throw new CustomException(HttpStatus.NOT_FOUND,"존재하지 않는 채팅방 입니다.");
        }
        
        int size = conversationDTO.getLimit() > 0 ? conversationDTO.getLimit() : 10;
        Long chatId = conversationDTO.getChatId() > 0 ? (long) conversationDTO.getChatId() : Long.MAX_VALUE;

        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());

        Page<ConversationResDTO> dtoPage = chatRepository.getConversationByChatId(
                conversationDTO.getUser1(),
                conversationDTO.getUser2(), 
                chatId, 
                pageable);

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
        // 간단한 검증만 남김
        if (userId == null || userId.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST,"존재하지 않는 회원 입니다.");
        }

        return chatRoomRepository.findMyChatRooms(userId);
    }
}