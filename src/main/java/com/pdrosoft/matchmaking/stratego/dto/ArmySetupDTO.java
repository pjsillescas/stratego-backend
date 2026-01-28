package com.pdrosoft.matchmaking.stratego.dto;

import java.util.List;

import com.pdrosoft.matchmaking.stratego.enums.Rank;
import com.pdrosoft.matchmaking.stratego.validation.ArmySetupValidation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmySetupDTO {
	@ArmySetupValidation
	private List<List<Rank>> army;
}
