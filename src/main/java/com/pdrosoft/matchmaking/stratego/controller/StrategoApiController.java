package com.pdrosoft.matchmaking.stratego.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdrosoft.matchmaking.security.payload.MatchmakingUserDetails;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.GameStateDTO;
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementDTO;
import com.pdrosoft.matchmaking.stratego.service.StrategoService;

import jakarta.validation.Valid;
import lombok.NonNull;

@RestController
@RequestMapping("/api/stratego/{gameId:[0-9]+}")
public class StrategoApiController {

	@NonNull
	private final StrategoService strategoService;

	public StrategoApiController(@Autowired StrategoService strategoService) {
		this.strategoService = strategoService;
	}

	@PutMapping(path = "/setup", produces = { "application/json" })
	public GameStateDTO addSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId, @RequestBody @Valid ArmySetupDTO setupDto) {
		System.out.println("apply setup");
		return strategoService.addSetup(gameId, userDetails.getPlayer(), setupDto);
	}

	@PutMapping(path = "/movement", produces = { "application/json" })
	public GameStateDTO addMovement(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId, @Valid @RequestBody StrategoMovementDTO movementDto) {
		return strategoService.addMovement(gameId, userDetails.getPlayer(), movementDto);
	}

	@GetMapping(path = "/status", produces = { "application/json" })
	public GameStateDTO getStatus(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId) {
		return strategoService.getStatus(gameId, userDetails.getPlayer());
	}

}
