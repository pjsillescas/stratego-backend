package com.pdrosoft.matchmaking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pdrosoft.matchmaking.model.StrategoMovement;

@Repository
public interface StrategoMovementRepository
		extends JpaRepository<StrategoMovement, Long>, JpaSpecificationExecutor<StrategoMovement> {

	default List<StrategoMovement> findAllByGameId(Long gameId) {
		return findAll((root, query, cb) -> cb.equal(root.get("game").get("id"), gameId));
	}
}
