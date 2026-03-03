package com.pdrosoft.matchmaking.chat.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.pdrosoft.matchmaking.chat.dto.ChatMessageDTO;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ChatController {

	@NonNull
	private final SimpMessagingTemplate messagingTemplate;

	/*
	//@MessageMapping("/game/{id}/chat")
	public void sendChat(@DestinationVariable Long id, ChatMessageDTO message, Principal principal) {

		message.setSender(principal.getName());

		messagingTemplate.convertAndSend("/topic/game/%d/chat".formatted(id), message);
	}
	*/
}