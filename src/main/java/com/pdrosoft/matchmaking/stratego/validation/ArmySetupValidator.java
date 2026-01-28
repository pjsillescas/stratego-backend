package com.pdrosoft.matchmaking.stratego.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdrosoft.matchmaking.stratego.enums.Rank;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArmySetupValidator implements ConstraintValidator<ArmySetupValidation, List<List<Rank>>> {

	private static final Integer NUM_MARSHALS = 1;
	private static final Integer NUM_GENERALS = 1;
	private static final Integer NUM_COLONELS = 2;
	private static final Integer NUM_MAJORS = 3;
	private static final Integer NUM_CAPTAINS = 4;
	private static final Integer NUM_LIEUTENANTS = 4;
	private static final Integer NUM_SERGEANTS = 4;
	private static final Integer NUM_MINERS = 5;
	private static final Integer NUM_SCOUTS = 8;
	
	private static final Integer NUM_SPIES = 1;
	private static final Integer NUM_BOMBS = 6;
	private static final Integer NUM_FLAGS = 1;
	
	private static final Integer NUM_ROWS = 4;
	private static final Integer NUM_RANKS_PER_ROW = 10;

	private Map<Rank,Integer> getValidSetup() {
		var setupMap = new HashMap<Rank, Integer>();
		setupMap.put(Rank.MARSHAL, NUM_MARSHALS);
		setupMap.put(Rank.GENERAL, NUM_GENERALS);
		setupMap.put(Rank.COLONEL, NUM_COLONELS);
		setupMap.put(Rank.MAJOR, NUM_MAJORS);
		setupMap.put(Rank.CAPTAIN, NUM_CAPTAINS);
		setupMap.put(Rank.LIEUTENANT, NUM_LIEUTENANTS);
		setupMap.put(Rank.SERGEANT, NUM_SERGEANTS);
		setupMap.put(Rank.MINER, NUM_MINERS);
		setupMap.put(Rank.SCOUT, NUM_SCOUTS);
		setupMap.put(Rank.SPY, NUM_SPIES);
		setupMap.put(Rank.BOMB, NUM_BOMBS);
		setupMap.put(Rank.FLAG, NUM_FLAGS);
		
		return setupMap;
	}

	@Override
	public boolean isValid(List<List<Rank>> army, ConstraintValidatorContext ctx) {
		
		if (army == null || army.size() != NUM_ROWS) {
			return false;
		}
		
		for(var row : army) {
			if (row == null || row.size() != NUM_RANKS_PER_ROW) {
				return false;
			}
		}
		
		var validSetup = getValidSetup();
		
		var stats = new HashMap<Rank,Integer>();
		for(var row : army) {
			for(var rank: row) {
				if(stats.containsKey(rank)) {
					stats.replace(rank, stats.get(rank) + 1);
				} else {
					stats.put(rank, 1);
				}
			}
		}

		for(var entry : stats.entrySet()) {
			var rank = entry.getKey();
			if(entry.getValue() != validSetup.get(rank)) {
				return false;
			}
		}

		return true;
	}

}
