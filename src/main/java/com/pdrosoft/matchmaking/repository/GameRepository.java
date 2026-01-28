package com.pdrosoft.matchmaking.repository;

import org.springframework.data.repository.CrudRepository;

import com.pdrosoft.matchmaking.model.Game;

public interface GameRepository extends CrudRepository<Game, Long> {

}
