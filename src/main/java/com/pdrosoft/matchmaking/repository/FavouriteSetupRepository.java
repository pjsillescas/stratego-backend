package com.pdrosoft.matchmaking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pdrosoft.matchmaking.model.FavouriteSetup;
import com.pdrosoft.matchmaking.model.Player;

public interface FavouriteSetupRepository extends JpaRepository<FavouriteSetup, Integer>, JpaSpecificationExecutor<FavouriteSetup> {
	
	public default List<FavouriteSetup> getSetupList(Player player) {
	    return findAll((root, query, builder) -> {
	        query.orderBy(builder.asc(root.get("description")));
	        return builder.and(
	                builder.equal(root.get("owner").get("id"), player.getId())
	        );
	    });
	}
}
