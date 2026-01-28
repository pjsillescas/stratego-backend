package com.pdrosoft.matchmaking.stratego.service;

import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.GameStateDTO;
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementDTO;

public interface StrategoService {

	GameStateDTO addSetup(Long gameId, Player player, ArmySetupDTO setupDto);

	GameStateDTO addMovement(Long gameId, Player player, StrategoMovementDTO movementDto);

	GameStateDTO getStatus(Long gameId, Player player);

}
