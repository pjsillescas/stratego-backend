package com.pdrosoft.matchmaking.stratego.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdrosoft.matchmaking.security.payload.MatchmakingUserDetails;
import com.pdrosoft.matchmaking.stratego.dto.FavouriteSetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.FavouriteSetupInputDTO;
import com.pdrosoft.matchmaking.stratego.service.FavouriteSetupService;

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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stratego/favourite")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Tag(name = "Favourite setups", description = "Favourite setups endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class FavouriteSetupApiController {

	@NonNull
	private final FavouriteSetupService favouriteSetupService;

	@Operation(summary = "Get setup list", description = "Get setup list")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FavouriteSetupDTO.class)))), //
			@ApiResponse(responseCode = "400", description = "invalid credentials", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FavouriteSetupDTO.class)))) //
	})
	@GetMapping(path = "/setup", produces = { "application/json" })
	public List<FavouriteSetupDTO> getFavouriteSetupList(@AuthenticationPrincipal MatchmakingUserDetails userDetails) {
		return favouriteSetupService.getSetupList(userDetails.getPlayer());
	}

	@Operation(summary = "Get setup by id", description = "Get setup by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup found successfully", content = @Content(schema = @Schema(implementation = FavouriteSetupDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@GetMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> getFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId) {
		return favouriteSetupService.getSetup(setupId, userDetails.getPlayer());
	}

	@Operation(summary = "Create setup", description = "Create setup")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup created successfully", content = @Content(schema = @Schema(implementation = FavouriteSetupDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/setup", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> addFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@RequestBody @Valid FavouriteSetupInputDTO favouriteSetupDto) {
		return favouriteSetupService.addSetup(favouriteSetupDto, userDetails.getPlayer());
	}

	@Operation(summary = "Update setup", description = "Update setup")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup updated successfully", content = @Content(schema = @Schema(implementation = FavouriteSetupDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> updateFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId, @Valid @RequestBody FavouriteSetupInputDTO favouriteSetupDto) {
		return favouriteSetupService.updateSetup(setupId, favouriteSetupDto, userDetails.getPlayer());
	}

	@Operation(summary = "Delete setup", description = "Delete setup")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup deleted successfully", content = @Content(schema = @Schema(implementation = FavouriteSetupDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@DeleteMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> deleteFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId) {
		return favouriteSetupService.deleteSetup(setupId, userDetails.getPlayer());
	}

}
