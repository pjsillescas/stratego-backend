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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;

@RestController
@RequestMapping("/api/stratego/{gameId:[0-9]+}")
@Tag(name = "Stratego gameplay", description = "Stratego endplay endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class StrategoApiController {

	@NonNull
	private final StrategoService strategoService;

	public StrategoApiController(@Autowired StrategoService strategoService) {
		this.strategoService = strategoService;
	}

	@Operation(summary = "Add player setup", description = "Add player setup")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup added successfully", content = @Content(schema = @Schema(implementation = GameStateDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/setup", produces = { "application/json" })
	public GameStateDTO addSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId, @RequestBody @Valid ArmySetupDTO setupDto) {
		System.out.println("apply setup");
		return strategoService.addSetup(gameId, userDetails.getPlayer(), setupDto);
	}

	@Operation(summary = "Add movement", description = "Add movement")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movement added successfully", content = @Content(schema = @Schema(implementation = GameStateDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/movement", produces = { "application/json" })
	public GameStateDTO addMovement(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId, @Valid @RequestBody StrategoMovementDTO movementDto) {
		return strategoService.addMovement(gameId, userDetails.getPlayer(), movementDto);
	}

	@Operation(summary = "Get status", description = "Get status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Game status found", content = @Content(schema = @Schema(implementation = GameStateDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@GetMapping(path = "/status", produces = { "application/json" })
	public GameStateDTO getStatus(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId) {
		return strategoService.getStatus(gameId, userDetails.getPlayer());
	}

}
