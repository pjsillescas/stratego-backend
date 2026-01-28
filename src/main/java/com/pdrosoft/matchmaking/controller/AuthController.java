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

import jakarta.validation.Valid;
import lombok.NonNull;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@NonNull
	private final AuthenticationManager authManager;

	@NonNull
	private final JwtUtil jwtUtil;

	@NonNull
	private final MatchmakingService matchmakingService;

	public AuthController(@Autowired AuthenticationManager authManager, @Autowired JwtUtil jwtUtil,
			@Autowired MatchmakingService matchmakingService) {
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
		this.matchmakingService = matchmakingService;
	}

	@PutMapping("/login")
	//@PostMapping("/login")
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

	@PutMapping("/signup")
	public PlayerDTO signup(@Valid @RequestBody UserAuthDTO request, Errors errors) {

		if (errors.hasErrors()) {
			throw new MatchmakingValidationException(errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)//
					.collect(Collectors.joining(", ")));
		}

		return matchmakingService.addPlayer(request.getUsername(), request.getPassword());
	}

}
