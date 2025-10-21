package com.chating.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
	// model mapper
	@Bean
	 public ModelMapper modelMapper() {
        return new ModelMapper();
    }
	
}
