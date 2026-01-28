package com.pdrosoft.matchmaking.stratego.dto;

import java.io.Serializable;
import java.util.List;

import com.pdrosoft.matchmaking.dto.PlayerDTO;
import com.pdrosoft.matchmaking.stratego.enums.GamePhase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateDTO implements Serializable {

	private static final long serialVersionUID = 3924386161467219822L;

	private PlayerDTO currentPlayer;
	private Long gameId;
	private Integer hostPlayerId;
	private Integer guestPlayerId;

	private StrategoMovementDTO movement;
	private GamePhase phase;

	private List<List<BoardTileDTO>> board;
	
	private boolean isMyTurn;
}
