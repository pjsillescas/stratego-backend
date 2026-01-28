package com.pdrosoft.matchmaking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pdrosoft.matchmaking.model.StrategoStatus;

@Repository
public interface StrategoStatusRepository
		extends JpaRepository<StrategoStatus, Long>, JpaSpecificationExecutor<StrategoStatus> {

	default Optional<StrategoStatus> findByGameId(Long gameId) {
		return findOne((root, query, cb) -> cb.equal(root.get("game").get("id"), gameId));
	}
}
