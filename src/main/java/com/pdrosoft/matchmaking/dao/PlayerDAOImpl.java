package com.pdrosoft.matchmaking.dao;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pdrosoft.matchmaking.model.Player;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class PlayerDAOImpl implements PlayerDAO {
	@PersistenceContext
	private EntityManager em;

	@Override
	public Optional<Player> findPlayersByName(String userName) {
		var cb = em.getCriteriaBuilder();
		var cq = cb.createQuery(Player.class);
		var root = cq.from(Player.class);
		cq.select(root).where(cb.equal(root.get("userName"), userName));

		return em.createQuery(cq).getResultStream().findAny();
	}

}
