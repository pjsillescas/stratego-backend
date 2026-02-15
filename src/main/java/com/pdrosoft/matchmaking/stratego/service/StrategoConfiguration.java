package com.pdrosoft.matchmaking.stratego.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class StrategoConfiguration {

	@Bean
	public ObjectMapper getObjectMapper() {
		var mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		// support Java 8 date time apis
		mapper.registerModule(new JavaTimeModule());

		return mapper;
	}

}
