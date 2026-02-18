package com.pdrosoft.matchmaking.stratego.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteSetupDTO {

	@Positive
	private Integer id;

	@NotEmpty
	private String description;

	@Valid
	private ArmySetupDTO armySetupDTO;
}
