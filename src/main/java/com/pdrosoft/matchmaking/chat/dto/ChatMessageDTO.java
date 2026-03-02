package com.pdrosoft.matchmaking.chat.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class ChatMessageDTO implements Serializable {

	private static final long serialVersionUID = -1346186732492932619L;

	private String message;
	private String sender;
	private Instant timestamp;
}
