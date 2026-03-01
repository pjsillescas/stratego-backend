package com.pdrosoft.matchmaking.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdrosoft.matchmaking.dto.LoginResultDTO;
import com.pdrosoft.matchmaking.dto.PlayerDTO;
import com.pdrosoft.matchmaking.dto.UserAuthDTO;
import com.pdrosoft.matchmaking.exception.MatchmakingValidationException;
import com.pdrosoft.matchmaking.security.JwtUtil;
import com.pdrosoft.matchmaking.service.MatchmakingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Tag(name = "API Authorization", description = "Authorization management endpoints")
public class AuthController {

	@NonNull
	private final AuthenticationManager authManager;

	@NonNull
	private final JwtUtil jwtUtil;

	@NonNull
	private final MatchmakingService matchmakingService;

	@Operation(summary = "Player login", description = "Player login")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Player logged in successfully", content = @Content(schema = @Schema(implementation = UserAuthDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping("/login")
	// @PostMapping("/login")
	public LoginResultDTO login(@Valid @RequestBody UserAuthDTO request, Errors errors) {
		if (errors.hasErrors()) {
			throw new MatchmakingValidationException(errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)//
					.collect(Collectors.joining(", ")));
		}
		Authentication auth = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		if (auth.isAuthenticated()) {
			String token = jwtUtil.generateToken(auth.getName());
			return LoginResultDTO.builder().token(token).build();
		} else {
			throw new BadCredentialsException("Invalid credentials");
		}
	}

	@Operation(summary = "Player sign up", description = "Player sign up")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Player created successfully", content = @Content(schema = @Schema(implementation = PlayerDTO.class))), //
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema())), //
			@ApiResponse(responseCode = "404", description = "invalid credentials", content = @Content(schema = @Schema())) //
	})
	@PutMapping("/signup")
	public PlayerDTO signup(@Valid @RequestBody UserAuthDTO request, Errors errors) {

		if (errors.hasErrors()) {
			throw new MatchmakingValidationException(errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)//
					.collect(Collectors.joining(", ")));
		}

		return matchmakingService.addPlayer(request.getUsername(), request.getPassword());
	}

}
