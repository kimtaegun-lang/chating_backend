package com.chating.entity.chat;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name="chat_room")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long roomId; // 채팅방 번호
	
	@Column(nullable=false)
	private String user1; // 회원1
	
	@Column(nullable=false)
    private String user2; // 회원2
	
	@Column(nullable=false)
	private LocalDateTime createdAt; // 방 개설 시간
	
	@OneToMany(mappedBy="chatroom" ,cascade=CascadeType.REMOVE)
	@ToString.Exclude
	private List<Chat> chat; // 채팅 내역
}
