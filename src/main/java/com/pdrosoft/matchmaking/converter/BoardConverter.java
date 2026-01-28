package com.pdrosoft.matchmaking.converter;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdrosoft.matchmaking.stratego.dto.BoardTileDTO;

import jakarta.persistence.AttributeConverter;

public class BoardConverter implements AttributeConverter<List<List<BoardTileDTO>>, String> {

	private static ObjectMapper objectMapper = null;

	private static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}

		return objectMapper;
	}

	@Override
	public String convertToDatabaseColumn(List<List<BoardTileDTO>> board) {
		String customerInfoJson = null;
		try {
			customerInfoJson = getObjectMapper().writeValueAsString(board);
		} catch (final JsonProcessingException e) {
			// logger.error("JSON writing error", e);
		}

		return customerInfoJson;
	}

	@Override
	public List<List<BoardTileDTO>> convertToEntityAttribute(String json) {
		List<List<BoardTileDTO>> customerInfo = null;
		try {
			customerInfo = getObjectMapper().readValue(json, //
					new TypeReference<List<List<BoardTileDTO>>>() {
					});
		} catch (final IOException e) {
			// logger.error("JSON reading error", e);
		}

		return customerInfo;
	}

}
