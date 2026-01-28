package com.pdrosoft.matchmaking.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pdrosoft.matchmaking.dto.ErrorResultDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private ErrorResultDTO getErrorObject(String message) {
		return ErrorResultDTO.builder().timestamp(Instant.now()).message(message).build();
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResultDTO> handleNotFound(NotFoundException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MatchmakingValidationException.class)
	public ResponseEntity<ErrorResultDTO> handleBadRequest(MatchmakingValidationException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResultDTO> handleBadRequest(MethodArgumentNotValidException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResultDTO> handleBadRequest(UsernameNotFoundException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResultDTO> handleBadRequest(BadCredentialsException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(PlayerExistsException.class)
	public ResponseEntity<ErrorResultDTO> handleBadRequest(PlayerExistsException ex) {
		var body = getErrorObject(ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResultDTO> handleGeneric(Exception ex) {
		var body = getErrorObject("Internal server error");
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}