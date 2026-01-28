package com.pdrosoft.matchmaking.dto;

import java.time.Instant;

import com.pdrosoft.matchmaking.stratego.enums.GamePhase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameExtendedDTO {

	private Integer id;

	private String name;

	private Instant creationDate;

	private PlayerDTO host;
	private PlayerDTO guest;

	private String joinCode;

	private GamePhase phase;
}
