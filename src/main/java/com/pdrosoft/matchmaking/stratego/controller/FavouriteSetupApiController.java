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
import com.pdrosoft.matchmaking.stratego.service.FavouriteSetupService;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stratego/favourite")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class FavouriteSetupApiController {

	@NonNull
	private final FavouriteSetupService favouriteSetupService;

	@GetMapping(path = "/setup", produces = { "application/json" })
	public List<FavouriteSetupDTO> getFavouriteSetupList(@AuthenticationPrincipal MatchmakingUserDetails userDetails) {
		return favouriteSetupService.getSetupList(userDetails.getPlayer());
	}

	@GetMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> getFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId) {
		return favouriteSetupService.getSetup(setupId, userDetails.getPlayer());
	}

	@PutMapping(path = "/setup", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> addFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@RequestBody @Valid FavouriteSetupDTO favouriteSetupDto) {
		return favouriteSetupService.addSetup(favouriteSetupDto, userDetails.getPlayer());
	}

	@PutMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> updateFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId, @Valid @RequestBody FavouriteSetupDTO favouriteSetupDto) {
		return favouriteSetupService.updateSetup(setupId, favouriteSetupDto, userDetails.getPlayer());
	}

	@DeleteMapping(path = "/setup/{setupId:[0-9]+}", produces = { "application/json" })
	public Optional<FavouriteSetupDTO> deleteFavouriteSetup(@AuthenticationPrincipal MatchmakingUserDetails userDetails,
			@PathVariable("setupId") Integer setupId) {
		return favouriteSetupService.deleteSetup(setupId, userDetails.getPlayer());
	}

}
