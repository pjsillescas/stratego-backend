package com.pdrosoft.matchmaking.service;

import java.time.Instant;
import java.util.List;

import com.pdrosoft.matchmaking.dto.GameDTO;
import com.pdrosoft.matchmaking.dto.GameExtendedDTO;
import com.pdrosoft.matchmaking.dto.GameInputDTO;
import com.pdrosoft.matchmaking.model.Player;

public interface GameService {
	List<GameDTO> getGameList(Instant dateFrom);

	GameDTO addGame(Player host, GameInputDTO gameInputDto);

	GameExtendedDTO joinGame(Player guest, Long gameId);

	GameDTO leaveGame(Player leavingPlayer, Long gameId);

	GameExtendedDTO getGame(Player guest, Long gameId);
}
