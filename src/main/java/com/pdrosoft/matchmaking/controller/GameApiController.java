package com.pdrosoft.matchmaking.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pdrosoft.matchmaking.dto.GameDTO;
import com.pdrosoft.matchmaking.dto.GameExtendedDTO;
import com.pdrosoft.matchmaking.dto.GameInputDTO;
import com.pdrosoft.matchmaking.security.payload.MatchmakingUserDetails;
import com.pdrosoft.matchmaking.service.MatchmakingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;

@RestController
@RequestMapping("/api")
@Tag(name = "Game management", description = "Game management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class GameApiController {

	@NonNull
	private final MatchmakingService matchmakingService;

	public GameApiController(@Autowired MatchmakingService matchmakingService) {
		this.matchmakingService = matchmakingService;
	}

	@Operation(summary = "Get game list", description = "Get game list")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Games found successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameDTO.class)))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@GetMapping(path = "/game", produces = { "application/json" })
	public List<GameDTO> getGames(@RequestParam(name = "date_from", required = false) String dateFromStr) {
		var dateFrom = Optional.ofNullable(dateFromStr).map(Instant::parse) //
				.orElse(Instant.now().minus(Duration.ofMinutes(10)));
		return matchmakingService.getGameList(dateFrom);
	}

	@Operation(summary = "Create game", description = "Create game")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Game created successfully", content = @Content(schema = @Schema(implementation = GameDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/game", produces = { "application/json" })
	public GameDTO addGame(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@Valid @RequestBody GameInputDTO gameInputDto) {
		return matchmakingService.addGame(userDetails.getPlayer(), gameInputDto);
	}

	@Operation(summary = "Get game by id", description = "Get game by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Game found successfully", content = @Content(schema = @Schema(implementation = GameExtendedDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@GetMapping(path = "/game/{gameId:[0-9]+}", produces = { "application/json" })
	public GameExtendedDTO getGame(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId) {
		return matchmakingService.getGame(userDetails.getPlayer(), gameId);
	}

	@Operation(summary = "Join game", description = "Join game")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Game joined successfully", content = @Content(schema = @Schema(implementation = GameExtendedDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/game/{gameId:[0-9]+}/join", produces = { "application/json" })
	// @PostMapping(path = "/game/{gameId:[0-9]+}/join", produces = {
	// "application/json" })
	public GameExtendedDTO joinGame(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId) {
		return matchmakingService.joinGame(userDetails.getPlayer(), gameId);
	}

	@Operation(summary = "Leave game", description = "Leave game")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Game left successfully", content = @Content(schema = @Schema(implementation = GameDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/game/{gameId:[0-9]+}/leave", produces = { "application/json" })
	// @PostMapping(path = "/game/{gameId:[0-9]+}/leave", produces = {
	// "application/json" })
	public GameDTO leaveGame(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("gameId") Long gameId) {
		return matchmakingService.leaveGame(userDetails.getPlayer(), gameId);
	}
}
