package com.chating.service.chat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.chating.entity.chat.Type;
import com.chating.entity.member.Member;
import com.chating.repository.chat.ChatRepository;
import com.chating.repository.chat.ChatRoomRepository;
import com.chating.repository.member.MemberRepository;
import com.chating.util.file.S3FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final ModelMapper modelMapper;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RabbitTemplate rabbitTemplate;
    private final FanoutExchange chatFanoutExchange;
    private final FanoutExchange notificationFanoutExchange;
    private final S3FileUtil s3FileUtil;
	private static final List<String> IMAGE_EXTENSIONS = List.of(
	        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff"
	);
	@Override
	public void saveMessage(sendMessageDTO message) {

	    // 1. 채팅방 검증
	    ChatRoom chatRoom = chatRoomRepository.findRoomByIds(
	            message.getSender(),
	            message.getReceiver())
	        .orElseThrow(() -> new CustomException(
	            HttpStatus.NOT_FOUND,
	            "채팅방 없음"
	        ));

	    MultipartFile file = message.getFile();

	    Chat.ChatBuilder builder = Chat.builder()
	            .sender(message.getSender())
	            .receiver(message.getReceiver())
	            .chatroom(chatRoom)
	            .createdAt(LocalDateTime.now());

	    // 2. 파일 처리
	    if (file != null && !file.isEmpty()) {

	        String uploadedUrl;
	        try {
	            uploadedUrl = s3FileUtil.upload(file);
	        } catch (IOException e) {
	            throw new CustomException(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "파일 업로드 실패"
	            );
	        }

	        String filename = file.getOriginalFilename();
	        long fileSize = file.getSize();

	        // 이미지인지 판단
	        Type detectedType = isImageFile(filename) ? Type.IMAGE : Type.FILE;

	        builder
	            .type(detectedType)
	            .url(uploadedUrl)
	            .fileName(filename)
	            .fileSize(fileSize);

	    } else {
	        // 텍스트 메시지
	        builder
	            .type(Type.TEXT)
	            .content(message.getContent());
	    }

	    // 3. 엔티티 저장
	    Chat savedChat = chatRepository.save(builder.build());

	    // 4. 응답 DTO 구성
	    BroadcastResDTO response = BroadcastResDTO.builder()
	        .chatId(savedChat.getChatId())
	        .chatRoomId(message.getRoomId())
	        .sender(savedChat.getSender())
	        .receiver(savedChat.getReceiver())
	        .content(savedChat.getContent())
	        .createdAt(savedChat.getCreatedAt())
	        .type(savedChat.getType().name()) // "TEXT", "FILE", "IMAGE"
	        .build();

	    if (savedChat.getType() != Type.TEXT) {
	        response.setUrl(savedChat.getUrl());
	        response.setFileName(savedChat.getFileName());
	        response.setFileSize(savedChat.getFileSize());
	    }

	    // 5. Fanout 브로드캐스트
	    rabbitTemplate.convertAndSend(chatFanoutExchange.getName(), "", response);
	    rabbitTemplate.convertAndSend(notificationFanoutExchange.getName(), "", response);

	}
    
    // 이미지 여부 판별
    private boolean isImageFile(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith);
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
    @Autowired
    public ChatServiceImpl(
            ModelMapper modelMapper,
            ChatRepository chatRepository,
            ChatRoomRepository chatRoomRepository,
            MemberRepository memberRepository,
            RabbitTemplate rabbitTemplate,
            @Qualifier("chatFanoutExchange") FanoutExchange chatFanoutExchange,
            @Qualifier("notificationFanoutExchange") FanoutExchange notificationFanoutExchange,
            S3FileUtil s3FileUtil
    ) {
        this.modelMapper = modelMapper;
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.chatFanoutExchange = chatFanoutExchange;
        this.notificationFanoutExchange = notificationFanoutExchange;
        this.s3FileUtil = s3FileUtil;
    }
    
}