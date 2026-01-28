package com.pdrosoft.matchmaking.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.pdrosoft.matchmaking.model.Player;

public interface PlayerRepository extends CrudRepository<Player, Long> {
	default Optional<Player> findByPK(Long id) {
		return this.findById(id);
	}
}
