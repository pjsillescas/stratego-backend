package com.pdrosoft.matchmaking.stratego.dto;

import com.pdrosoft.matchmaking.stratego.enums.Rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategoMovementResultDTO {

	private Rank rank;
	private boolean isHost;
}
