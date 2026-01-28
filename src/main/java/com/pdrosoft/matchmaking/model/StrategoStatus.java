package com.pdrosoft.matchmaking.model;

import java.util.List;

import com.pdrosoft.matchmaking.converter.BoardConverter;
import com.pdrosoft.matchmaking.stratego.dto.BoardTileDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "stratego_status")
public class StrategoStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;
	
	@Column(name = "is_guest_turn", nullable = false)
	private Boolean isGuestTurn;

	@Lob
	@Column(nullable = false)
	@Convert(converter = BoardConverter.class)
	private List<List<BoardTileDTO>> board;
	
	@Column(name = "is_host_initialized",nullable = false)
	private Boolean isHostInitialized;

	@Column(name = "is_guest_initialized",nullable = false)
	private Boolean isGuestInitialized;
}
