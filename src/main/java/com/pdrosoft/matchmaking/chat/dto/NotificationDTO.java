package com.pdrosoft.matchmaking.chat.dto;

import com.pdrosoft.matchmaking.stratego.enums.GamePhase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

	private GamePhase gamePhase;
	private String message;
}
