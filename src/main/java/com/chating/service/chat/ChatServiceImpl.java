package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import com.chating.entity.member.Member;
import com.chating.repository.chat.ChatRepository;
import com.chating.repository.chat.ChatRoomRepository;
import com.chating.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final ModelMapper modelMapper;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RabbitTemplate rabbitTemplate;
    private final FanoutExchange chatFanoutExchange;

    @Override
    public void saveMessage(sendMessageDTO message) {
        ChatRoom chatRoom = chatRoomRepository.findRoomByIds(
                message.getSender(), message.getReceiver())
            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "채팅방 없음"));

        Chat chat = Chat.builder()
                .sender(message.getSender())
                .receiver(message.getReceiver())
                .content(message.getContent())
                .createdAt(LocalDateTime.now())
                .chatroom(chatRoom)
                .build();

        Chat savedChat = chatRepository.save(chat);
        BroadcastResDTO response = modelMapper.map(savedChat, BroadcastResDTO.class);
        response.setChatRoomId(message.getRoomId());
        response.setType("CREATE");

        // Fanout Exchange로 발행 (모든 서버에 브로드캐스트)
        // routingKey는 빈 문자열 (Fanout은 라우팅 키 무시)
        rabbitTemplate.convertAndSend(chatFanoutExchange.getName(), "", response);
    }

    @Transactional
    public void deleteChat(DeleteMessageDTO message) {
        Chat chat = chatRepository.findById(message.getChatId())
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "해당 채팅을 찾을 수 없습니다."
            ));

        chatRoomRepository.findById(message.getRoomId())
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "유효하지 않은 채팅방 입니다."
            ));

        chatRepository.deleteById(message.getChatId());

        BroadcastResDTO response = BroadcastResDTO.builder()
            .chatId(message.getChatId())
            .chatRoomId(message.getRoomId())
            .type("DELETE")
            .build();

        // Fanout Exchange로 발행 (모든 서버에 브로드캐스트)
        rabbitTemplate.convertAndSend(chatFanoutExchange.getName(), "", response);
        
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ConversationResDTO> getConversation(ConversationDTO conversationDTO) {
        ChatRoom chatRoom = chatRoomRepository.findRoomByIds(
                conversationDTO.getUser1(),
                conversationDTO.getUser2()
            )
            .orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND,
                "회원들간 채팅방을 찾을 수 없습니다."
            ));

        if (chatRoom.getRoomId() != conversationDTO.getRoomId()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방 입니다.");
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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ChatRoomResDTO> getMyChatRooms(String userId, int pageCount, int size) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원 입니다.");
        }

        Pageable pageable = PageRequest.of(pageCount, size);
        Page<ChatRoom> chatRoomPage = chatRoomRepository.findMyChatRooms(pageable, userId);

        Page<ChatRoomResDTO> dtoPage = chatRoomPage.map(chatroom -> {
            ChatRoomResDTO dto = modelMapper.map(chatroom, ChatRoomResDTO.class);

            if (userId.equals(chatroom.getUser1())) {
                dto.setReceiver(chatroom.getUser2());
            } else {
                dto.setReceiver(chatroom.getUser1());
            }

            return dto;
        });

        return new PageResponseDTO<>(dtoPage);
    }

    public boolean getReceiverStatus(String receiverId) {
        Optional<Member> member = memberRepository.findById(receiverId);
        return member.isPresent();
    }
}