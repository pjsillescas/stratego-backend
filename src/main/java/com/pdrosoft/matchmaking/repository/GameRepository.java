package com.pdrosoft.matchmaking.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pdrosoft.matchmaking.model.Game;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
	
	public default List<Game> getGameList(Instant dateFrom) {
	    return findAll((root, query, builder) -> {
	        query.orderBy(builder.desc(root.get("creationDate")));
	        return builder.and(
	                builder.isNull(root.get("guest")),
	                builder.greaterThan(root.get("creationDate"), dateFrom)
	        );
	    });
	}
}
