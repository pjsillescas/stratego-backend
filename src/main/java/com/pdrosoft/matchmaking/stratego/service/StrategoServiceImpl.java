package com.pdrosoft.matchmaking.stratego.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pdrosoft.matchmaking.dao.GameDAO;
import com.pdrosoft.matchmaking.dao.PlayerDAO;
import com.pdrosoft.matchmaking.dto.PlayerDTO;
import com.pdrosoft.matchmaking.exception.MatchmakingValidationException;
import com.pdrosoft.matchmaking.model.Game;
import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.model.StrategoMovement;
import com.pdrosoft.matchmaking.model.StrategoStatus;
import com.pdrosoft.matchmaking.repository.GameRepository;
import com.pdrosoft.matchmaking.repository.PlayerRepository;
import com.pdrosoft.matchmaking.repository.StrategoMovementRepository;
import com.pdrosoft.matchmaking.repository.StrategoStatusRepository;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.BoardTileDTO;
import com.pdrosoft.matchmaking.stratego.dto.GameStateDTO;
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementDTO;
import com.pdrosoft.matchmaking.stratego.enums.GamePhase;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Service
public class StrategoServiceImpl implements StrategoService {

	@NonNull
	private final GameDAO gameDao;
	@NonNull
	private final GameRepository gameRepository;
	@NonNull
	private final PlayerRepository playerRepository;
	@NonNull
	private final PlayerDAO playerDao;
	@NonNull
	private final PasswordEncoder passwordEncoder;
	@NonNull
	private final StrategoStatusRepository strategoStatusRepository;
	@NonNull
	private final StrategoMovementRepository strategoMovementRepository;
	@NonNull
	private final RankService rankService;

	private PlayerDTO toPlayerDTO(Player player) {
		return PlayerDTO.builder().id(player.getId()).username(player.getUserName()).build();

	}

	private List<List<BoardTileDTO>> getEmptyBoard() {
		var disa = BoardTileDTO.builder().rank(Rank.DISABLED).build();
		var row1 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row2 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row3 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row4 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row5 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, disa, disa, null, null, disa, disa, null, null));
		var row6 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, disa, disa, null, null, disa, disa, null, null));
		var row7 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row8 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row9 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));
		var row10 = new ArrayList<BoardTileDTO>(
				Arrays.asList(null, null, null, null, null, null, null, null, null, null));

		var board = new ArrayList<List<BoardTileDTO>>(
				Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10));

		return board;
	}

	private void copySetupHost(ArmySetupDTO setupDto, List<List<BoardTileDTO>> board) {
		var offset = 0;
		for (int iRow = 0; iRow <= 3; iRow++) {
			var setupRow = setupDto.getArmy().get(iRow);
			var boardRow = board.get(offset + iRow);

			for (int iCol = 0; iCol < 10; iCol++) {
				var tile = BoardTileDTO.builder().rank(setupRow.get(iCol)).isHostOwner(true).build();
				boardRow.set(iCol, tile);
			}
		}
	}

	private void copySetupGuest(ArmySetupDTO setupDto, List<List<BoardTileDTO>> board) {
		for (int iRow = 0; iRow <= 3; iRow++) {
			var setupRow = setupDto.getArmy().get(iRow);
			var boardRow = board.get(9 - iRow);

			for (int iCol = 0; iCol < 10; iCol++) {
				var tile = BoardTileDTO.builder().rank(setupRow.get(9 - iCol)).isHostOwner(false).build();
				boardRow.set(iCol, tile);
			}
		}
	}

	private StrategoStatus getNewStrategoGame(Game game) {
		var status = new StrategoStatus();

		status.setBoard(getEmptyBoard());
		status.setGame(game);
		status.setIsGuestTurn(false);
		status.setIsHostInitialized(false);
		status.setIsGuestInitialized(false);

		return strategoStatusRepository.save(status);
	}

	private boolean isPlayerId(Integer playerId, Player player2) {
		return Optional.ofNullable(player2).map(Player::getId).filter(id -> id == playerId).isPresent();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public GameStateDTO addSetup(Long gameId, Player player, @Valid ArmySetupDTO setupDto) {

		var game = gameRepository.findById(gameId)
				.orElseThrow(() -> new MatchmakingValidationException("Game does not exist"));
		System.out.println("find game " + game.getId());

		var setupPhases = List.of(GamePhase.WAITING_FOR_SETUP_1_PLAYER, GamePhase.WAITING_FOR_SETUP_2_PLAYERS);

		if (game.getPhase() != null && !setupPhases.contains(game.getPhase())) {
			System.out.println("game not in setup state");

			throw new MatchmakingValidationException("Game not in setup state");
		}

		List<List<BoardTileDTO>> board = null;
		var status = strategoStatusRepository.findByGameId(gameId).orElseGet(() -> getNewStrategoGame(game));
		System.out.println("find status " + status.getId());

		board = status.getBoard();

		var isHost = isPlayerId(player.getId(), game.getHost());
		var isGuest = isPlayerId(player.getId(), game.getGuest());

		if (isHost && !status.getIsHostInitialized()) {
			status.setIsHostInitialized(true);
			copySetupHost(setupDto, board);
		} else if (isGuest && !status.getIsGuestInitialized()) {
			status.setIsGuestInitialized(true);
			copySetupGuest(setupDto, board);
		} else {
			System.out.println("invalid player setup");

			throw new MatchmakingValidationException("Invalid player setup");
		}

		status.setBoard(board);

		if (GamePhase.WAITING_FOR_SETUP_1_PLAYER.equals(game.getPhase())) {
			game.setPhase(GamePhase.PLAYING);
		} else { // if (GamePhase.WAITING_FOR_SETUP_2_PLAYERS.equals(game.getPhase())) {
			game.setPhase(GamePhase.WAITING_FOR_SETUP_1_PLAYER);
		}
		
		System.out.println("setting phase " + game.getPhase());

		gameRepository.save(game);
		System.out.println("save game");

		strategoStatusRepository.save(status);
		System.out.println("save status");
		return GameStateDTO.builder() //
				.currentPlayer(toPlayerDTO(player)) //
				.hostPlayerId(Optional.ofNullable(game.getHost()).map(Player::getId).orElse(0)) //
				.guestPlayerId(Optional.ofNullable(game.getGuest()).map(Player::getId).orElse(0)) //
				.gameId(gameId) //
				.phase(game.getPhase()) //
				.movement(null) //
				.board(board) //
				.isMyTurn(isHost) //
				.build();
	}

	private boolean getIsMyTurn(Integer playerId, Game game, StrategoStatus status) {
		var isHost = isPlayerId(playerId, game.getHost());
		var isGuest = isPlayerId(playerId, game.getGuest());

		return isHost && !status.getIsGuestTurn() || isGuest && status.getIsGuestTurn();
	}

	private void checkValidMovement(StrategoMovementDTO movementDto, Game game, StrategoStatus status,
			Integer playerId) {

		var isMyTurn = getIsMyTurn(playerId, game, status);
		if (!isMyTurn) {
			throw new MatchmakingValidationException("Invalid player turn");
		}

		var isHost = isPlayerId(playerId, game.getHost());
		List<List<BoardTileDTO>> board = status.getBoard();
		var initialTile = board.get(movementDto.getRowInitial()).get(movementDto.getColInitial());
		if (initialTile == null || initialTile.isHostOwner() != isHost || Rank.DISABLED.equals(initialTile.getRank())) {
			throw new MatchmakingValidationException("Invalid chosen square");
		}

		if (isInmobileRank(initialTile.getRank())) {
			throw new MatchmakingValidationException("This square cannot move");
		}

		var finalTile = board.get(movementDto.getRowFinal()).get(movementDto.getColFinal());
		if (finalTile != null && (finalTile.isHostOwner() == isHost || Rank.DISABLED.equals(finalTile.getRank()))) {
			throw new MatchmakingValidationException("Invalid destination square");
		}
	}

	private void applyMovement(StrategoMovementDTO movementDto, List<List<BoardTileDTO>> board) {
		var initialTile = board.get(movementDto.getRowInitial()).get(movementDto.getColInitial());
		var finalTile = board.get(movementDto.getRowFinal()).get(movementDto.getColFinal());
		if (finalTile != null) {
			var result = rankService.compareRanks(initialTile.getRank(), finalTile.getRank());

			if (result < 0) {
				// player lost, destination tile stays
				setBoardPosition(board, movementDto.getRowInitial(), movementDto.getColInitial(), null);
				;
			} else if (result == 0) {
				// Tie, both squares are deleted
				setBoardPosition(board, movementDto.getRowInitial(), movementDto.getColInitial(), null);
				setBoardPosition(board, movementDto.getRowFinal(), movementDto.getColFinal(), null);
			} else { // result > 0
				// player won
				setBoardPosition(board, movementDto.getRowInitial(), movementDto.getColInitial(), null);
				setBoardPosition(board, movementDto.getRowFinal(), movementDto.getColFinal(), initialTile);
			}
		} else {
			// empty final tile, move directly
			setBoardPosition(board, movementDto.getRowInitial(), movementDto.getColInitial(), null);
			setBoardPosition(board, movementDto.getRowFinal(), movementDto.getColFinal(), initialTile);
		}
	}

	private boolean isInmobileRank(Rank rank) {
		var inmobileRanks = List.of(Rank.BOMB, Rank.DISABLED, Rank.FLAG);
		return inmobileRanks.contains(rank);
	}

	private void setBoardPosition(List<List<BoardTileDTO>> board, Integer row, Integer col, BoardTileDTO tile) {
		board.get(row).set(col, tile);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public GameStateDTO addMovement(Long gameId, Player player, @Valid StrategoMovementDTO movementDto) {

		var game = gameRepository.findById(gameId)
				.orElseThrow(() -> new MatchmakingValidationException("Game does not exist"));

		if (!GamePhase.PLAYING.equals(game.getPhase())) {
			throw new MatchmakingValidationException("Game not in playing state");
		}

		var status = strategoStatusRepository.findByGameId(gameId)
				.orElseThrow(() -> new MatchmakingValidationException("Game has not been started"));

		checkValidMovement(movementDto, game, status, player.getId());

		var board = status.getBoard();
		applyMovement(movementDto, board);
		status.setBoard(board);

		var isGuestTurn = status.getIsGuestTurn();
		status.setIsGuestTurn(!isGuestTurn);
		strategoStatusRepository.save(status);

		var move = new StrategoMovement();
		move.setGame(game);
		move.setRank(movementDto.getRank());
		move.setRowInitial(movementDto.getRowInitial());
		move.setColInitial(movementDto.getColInitial());
		move.setRowFinal(movementDto.getRowFinal());
		move.setColFinal(movementDto.getColFinal());
		move.setIsGuestTurn(isGuestTurn);

		strategoMovementRepository.save(move);

		return GameStateDTO.builder() //
				.currentPlayer(toPlayerDTO(player)) //
				.hostPlayerId(Optional.ofNullable(game.getHost()).map(Player::getId).orElse(0)) //
				.guestPlayerId(Optional.ofNullable(game.getGuest()).map(Player::getId).orElse(0)) //
				.gameId(gameId) //
				.phase(game.getPhase()) //
				.movement(movementDto) //
				.board(board) //
				.isMyTurn(false) //
				.build();
	}

	private StrategoMovementDTO toMovementDTO(StrategoMovement movement) {
		return StrategoMovementDTO.builder() //
				.rank(movement.getRank()) //
				.rowInitial(movement.getRowInitial()) //
				.rowFinal(movement.getRowFinal()) //
				.colInitial(movement.getColInitial()) //
				.colFinal(movement.getColFinal()) //
				.build();
	}

	@Override
	public GameStateDTO getStatus(Long gameId, Player player) {
		var game = gameRepository.findById(gameId)
				.orElseThrow(() -> new MatchmakingValidationException("Game does not exist"));
		System.out.println("get game " + game.getId());

		List<List<BoardTileDTO>> board = null;
		var status = strategoStatusRepository.findByGameId(gameId)
				.orElseThrow(() -> new MatchmakingValidationException("Game has not been started"));
		System.out.println("get status" + status.getId());

		board = status.getBoard();

		// var movement = strategoMovementRepository.findAllByGameId(gameId).getLast();
		var allMovements = strategoMovementRepository.findAllByGameId(gameId);
		var movement = Optional.ofNullable((allMovements == null || allMovements.size() == 0) ? null : allMovements.getLast());
		System.out.println("get movements");

		var isHost = player.equals(status.getGame().getHost());
		return GameStateDTO.builder() //
				.currentPlayer(toPlayerDTO(player)) //
				.hostPlayerId(Optional.ofNullable(game.getHost()).map(Player::getId).orElse(0)) //
				.guestPlayerId(Optional.ofNullable(game.getGuest()).map(Player::getId).orElse(0)) //
				.gameId(gameId) //
				.phase(game.getPhase()) //
				.movement(movement.map(this::toMovementDTO).orElse(null)) //
				.board(board) //
				.isMyTurn(isHost && !status.getIsGuestTurn() || !isHost && status.getIsGuestTurn()) //
				.build();
	}

}
