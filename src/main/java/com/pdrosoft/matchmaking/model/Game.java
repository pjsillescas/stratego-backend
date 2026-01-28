package com.pdrosoft.matchmaking.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.TimestampJdbcType;

import com.pdrosoft.matchmaking.stratego.enums.GamePhase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Game {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(name = "join_code", nullable = false)
	private String joinCode;

	@Column(name = "creation_date", nullable = false, unique = true)
	@JdbcType(TimestampJdbcType.class)
	private Instant creationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "host")
	private Player host;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "guest")
	private Player guest;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private GamePhase phase;
}
