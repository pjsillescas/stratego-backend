package com.pdrosoft.matchmaking.stratego.dto;

import com.pdrosoft.matchmaking.stratego.enums.Rank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategoMovementDTO {

	@NotNull
	private Rank rank;

	@NotNull
	@Min(0)
	@Max(9)
	private int rowInitial;
	@NotNull
	@Min(0)
	@Max(9)
	private int colInitial;

	@NotNull
	@Min(0)
	@Max(9)
	private int rowFinal;
	@NotNull
	@Min(0)
	@Max(9)
	private int colFinal;
}
