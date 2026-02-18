package com.pdrosoft.matchmaking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pdrosoft.matchmaking.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
	public default Optional<Player> findPlayersByName(String userName) {
		return this.findOne((from, q, cb) -> cb.equal(from.get("userName"), userName));
	}
}
