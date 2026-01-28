package com.pdrosoft.matchmaking.stratego.service;

import com.pdrosoft.matchmaking.stratego.enums.Rank;

public interface RankService {

	int compareRanks(Rank rankAttacker, Rank rankDefender);
}
