package com.chating.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.entity.chat.ChatRoom;

@Configuration
public class ModelMapperConfig {
	// model mapper
	// Configuration 클래스에서
	@Bean
	public ModelMapper modelMapper() {
	    ModelMapper modelMapper = new ModelMapper();
	    
	    return modelMapper;
	}
	
}
