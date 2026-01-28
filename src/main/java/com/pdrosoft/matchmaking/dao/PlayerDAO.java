package com.pdrosoft.matchmaking.dao;

import java.util.Optional;

import com.pdrosoft.matchmaking.model.Player;

public interface PlayerDAO {

	Optional<Player> findPlayersByName(String userName);

}
