package com.pdrosoft.matchmaking.stratego.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.pdrosoft.matchmaking.exception.MatchmakingValidationException;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

@Service
public class RankServiceImpl implements RankService {

	@Override
	public int compareRanks(Rank rankAttacker, Rank rankDefender) {
		var immobileRanks = List.of(Rank.BOMB, Rank.FLAG, Rank.DISABLED);
		if (immobileRanks.contains(rankAttacker) || Rank.DISABLED.equals(rankDefender)) {
			throw new MatchmakingValidationException("Invalid ranks compared");
		}

		if (Objects.equals(rankAttacker, rankDefender)) {
			return 0;
		}

		List<Rank> upperRanks = List.of();
		switch (rankDefender) {
		case FLAG:
			upperRanks = Arrays.asList(Rank.values());
			break;
		case BOMB:
			upperRanks = List.of(Rank.MINER);
			break;
		case MARSHAL:
			upperRanks = List.of(Rank.SPY);
			break;
		case GENERAL:
			upperRanks = List.of(Rank.MARSHAL);
			break;
		case COLONEL:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL);
			break;
		case MAJOR:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL);
			break;
		case CAPTAIN:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR);
			break;
		case LIEUTENANT:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR, Rank.CAPTAIN);
			break;
		case SERGEANT:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR, Rank.CAPTAIN, Rank.LIEUTENANT);
			break;
		case MINER:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR, Rank.CAPTAIN, Rank.LIEUTENANT,
					Rank.SERGEANT);
			break;
		case SCOUT:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR, Rank.CAPTAIN, Rank.LIEUTENANT,
					Rank.SERGEANT, Rank.MINER);
			break;
		case SPY:
			upperRanks = List.of(Rank.MARSHAL, Rank.GENERAL, Rank.COLONEL, Rank.MAJOR, Rank.CAPTAIN, Rank.LIEUTENANT,
					Rank.SERGEANT, Rank.MINER, Rank.SCOUT);
			break;
		default:
			throw new MatchmakingValidationException("Invalid ranks compared");
		}

		return upperRanks.contains(rankAttacker) ? 1 : -1;
	}
}
