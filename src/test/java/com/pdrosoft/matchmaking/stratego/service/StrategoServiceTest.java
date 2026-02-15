package com.pdrosoft.matchmaking.stratego.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementDTO;
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementResultDTO;
import com.pdrosoft.matchmaking.stratego.enums.GamePhase;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

@ExtendWith(MockitoExtension.class)
public class StrategoServiceTest {

	private static final Integer PLAYER_ID = 1;
	private static final String PLAYER_USERNAME = "player";
	private static final Integer HOST_ID = 2;
	private static final Integer GUEST_ID = 3;
	private static final Long GAME_ID = 10L;

	private static final Integer STATUS_ID = 1;

	@Mock
	private GameRepository gameRepository;
	@Mock
	private PlayerRepository playerRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private StrategoStatusRepository strategoStatusRepository;
	@Mock
	private StrategoMovementRepository strategoMovementRepository;
	@Mock
	private RankService rankService;
	@Mock
	private ObjectMapper mapper;

	@InjectMocks
	private StrategoServiceImpl strategoService;

	@Test
	void testGetStatusNoGame() {
		var player = getTestPlayer();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> strategoService.getStatus(GAME_ID, player))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game does not exist");
	}

	@Test
	void testGetStatusNoStatus() {
		var player = getTestPlayer();
		var game = getTestGame(player, player);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> strategoService.getStatus(GAME_ID, player))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game has not been started");
	}

	private static class MovementArgs implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of(List.of()), Arguments.of(List.of(getTestMovement())));
		}
	}

	@SuppressWarnings("unchecked")
	@ParameterizedTest
	@ArgumentsSource(value = MovementArgs.class)
	void testGetStatus(List<StrategoMovement> movements) throws JsonMappingException, JsonProcessingException {
		var player = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);
		var game = getTestGame(player, guest);
		var result = StrategoMovementResultDTO.builder().rank(Rank.MARSHAL).isHost(true).build();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

		var board = (List<List<BoardTileDTO>>) Mockito.mock(List.class);
		var status = Mockito.mock(StrategoStatus.class);
		Mockito.when(status.getGame()).thenReturn(game);
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));
		Mockito.when(status.getBoard()).thenReturn(board);

		Mockito.when(strategoMovementRepository.findAllByGameId(GAME_ID)).thenReturn(movements);

		if (movements.size() > 0) {
			Mockito.when(mapper.readValue(Mockito.anyString(), Mockito.any(TypeReference.class)))
					.thenReturn(List.of(result));
		}

		var statusDto = strategoService.getStatus(GAME_ID, player);

		assertThat(statusDto.getCurrentPlayer().getId()).isEqualTo(PLAYER_ID);
		assertThat(statusDto.getCurrentPlayer().getUsername()).isEqualTo(PLAYER_USERNAME);
		assertThat(statusDto.getHostPlayerId()).isEqualTo(PLAYER_ID);
		assertThat(statusDto.getGuestPlayerId()).isEqualTo(GUEST_ID);
		assertThat(statusDto.getGameId()).isEqualTo(GAME_ID);
		assertThat(statusDto.getPhase()).isEqualTo(GamePhase.PLAYING);
		assertThat(statusDto.getBoard()).isEqualTo(board);
		assertThat(statusDto.isMyTurn()).isTrue();

		if (movements.size() == 0) {
			assertThat(statusDto.getMovement()).isNull();
		} else {
			var movement = statusDto.getMovement();
			assertThat(movement.getRank()).isEqualTo(Rank.BOMB);
			assertThat(movement.getRowInitial()).isEqualTo(1);
			assertThat(movement.getColInitial()).isEqualTo(2);
			assertThat(movement.getRowFinal()).isEqualTo(3);
			assertThat(movement.getColFinal()).isEqualTo(4);
			assertThat(movement.getResult()).hasSize(1).contains(result);

			var captor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(mapper).readValue(captor.capture(), Mockito.any(TypeReference.class));
			assertThat(captor.getValue()).isEqualTo("[{\"rank\":\"MARSHAL\",\"isHost\":true}]");
		}
	}

	@Test
	void testAddMovementNoGame() {
		var player = getTestPlayer();
		var movementDto = getTestMovementDto();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game does not exist");
	}

	@ParameterizedTest
	@EnumSource(value = GamePhase.class, names = { "WAITING_FOR_SETUP_2_PLAYERS", "WAITING_FOR_SETUP_1_PLAYER",
			"FINISHED" })
	void testAddMovementWrongPlayerTurn(GamePhase wrongPhase) {
		var player = getTestPlayer();
		var game = getTestGame(player, player);
		game.setPhase(wrongPhase);
		var movementDto = getTestMovementDto();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game not in playing state");
	}

	@Test
	void testAddMovementNoStatus() {
		var player = getTestPlayer();
		var game = getTestGame(player, player);
		var movementDto = getTestMovementDto();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game has not been started");
	}

	private StrategoStatus getTestStatus(List<List<BoardTileDTO>> board, Game game) {
		var status = new StrategoStatus();
		status.setBoard(board);
		status.setGame(game);
		status.setId(STATUS_ID);
		status.setIsGuestInitialized(true);
		status.setIsHostInitialized(true);
		status.setIsGuestTurn(true);
		return status;
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void testAddMovementNotMyTurn(boolean isGuestTurn) {
		var host = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);
		var game = getTestGame(host, guest);
		var movementDto = getTestMovementDto();
		List<List<BoardTileDTO>> board = List.of();
		var status = getTestStatus(board, game);
		status.setIsGuestTurn(isGuestTurn);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));

		var movingPlayer = isGuestTurn ? host : guest;
		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, movingPlayer, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid player turn");
	}

	@Test
	void testAddMovementChosenSquare() {
		var player = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);

		var game = getTestGame(player, guest);
		var movementDto = getTestMovementDto();
		var tile = new BoardTileDTO(Rank.BOMB, true);
		List<List<BoardTileDTO>> board = List.of(
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, null, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }));
		var status = getTestStatus(board, game);
		status.setIsGuestTurn(false);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid chosen square");
	}

	@Test
	void testAddMovementChosenSquareCannotMove() {
		var player = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);
		var game = getTestGame(player, guest);
		var movementDto = getTestMovementDto();
		var tile = new BoardTileDTO(Rank.BOMB, true);
		List<List<BoardTileDTO>> board = List.of(
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }));
		var status = getTestStatus(board, game);
		status.setIsGuestTurn(false);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("This square cannot move");
	}

	private static class CannotMoveArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(new BoardTileDTO(Rank.DISABLED, false)), // Disabled tile
					Arguments.of(new BoardTileDTO(Rank.GENERAL, true)) // Same team tile
			);
		}

	}

	@ParameterizedTest
	@ArgumentsSource(value = CannotMoveArguments.class)
	void testAddMovementChosenSquareCannotMove(BoardTileDTO destinationTile) {
		var player = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);
		var game = getTestGame(player, guest);
		var movementDto = getTestMovementDto();
		var tile = new BoardTileDTO(Rank.SCOUT, true);
		List<List<BoardTileDTO>> board = List.of(
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(
						new BoardTileDTO[] { tile, tile, tile, tile, destinationTile, tile, tile, tile, tile, tile }));
		var status = getTestStatus(board, game);
		status.setIsGuestTurn(false);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));

		assertThatThrownBy(() -> strategoService.addMovement(GAME_ID, player, movementDto))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid destination square");
	}

	private static class AddMovementRanksArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, Rank.MARSHAL, null), // Same ranks
					Arguments.of(Rank.MARSHAL, Rank.GENERAL, Rank.MARSHAL), // Player wins
					Arguments.of(Rank.SCOUT, Rank.MARSHAL, Rank.MARSHAL), // Player loses
					Arguments.of(Rank.SCOUT, null, Rank.SCOUT) // empty destination
			);
		}

	}

	@SuppressWarnings("unchecked")
	@ParameterizedTest
	@ArgumentsSource(value = AddMovementRanksArguments.class)
	void testAddMovement(Rank initialRank, Rank destinationRank, Rank finalRank) throws JsonProcessingException {
		var player = getTestPlayer();
		var guest = getTestPlayer(GUEST_ID);

		var game = getTestGame(player, guest);
		var movementDto = getTestMovementDto();
		var tile = new BoardTileDTO(Rank.SCOUT, true);
		var initialTile = new BoardTileDTO(initialRank, true);
		BoardTileDTO destinationTile = Optional.ofNullable(destinationRank).map(rank -> new BoardTileDTO(rank, false))
				.orElse(null);
		List<List<BoardTileDTO>> board = List.of(
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, initialTile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(new BoardTileDTO[] { tile, tile, tile, tile, tile, tile, tile, tile, tile, tile }),
				Arrays.asList(
						new BoardTileDTO[] { tile, tile, tile, tile, destinationTile, tile, tile, tile, tile, tile }) //
		);
		var status = getTestStatus(board, game);
		status.setIsGuestTurn(false);

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(status));
		List<StrategoMovementResultDTO> result = List.of();
		if (destinationRank != null) {
			Mockito.when(rankService.compareRanks(initialRank, destinationRank)).thenAnswer(inv -> {
				Rank initRank = inv.getArgument(0);
				Rank destRank = inv.getArgument(1);

				if (Rank.MARSHAL.equals(initRank) && Rank.MARSHAL.equals(destRank)) {
					return 0;
				} else if (Rank.MARSHAL.equals(initRank) && Rank.GENERAL.equals(destRank)) {
					return 1;
				} else if (Rank.SCOUT.equals(initRank) && Rank.MARSHAL.equals(destRank)) {
					return -1;
				} else {
					return 0;
				}
			});

			if (Rank.MARSHAL.equals(initialRank) && Rank.MARSHAL.equals(destinationRank)) {
				result = List.of(StrategoMovementResultDTO.builder().rank(Rank.MARSHAL).isHost(true).build(),
						StrategoMovementResultDTO.builder().rank(Rank.MARSHAL).isHost(false).build());
			} else if (Rank.MARSHAL.equals(initialRank) && Rank.GENERAL.equals(destinationRank)) {
				result = List.of(StrategoMovementResultDTO.builder().rank(Rank.GENERAL).isHost(false).build());
			} else if (Rank.SCOUT.equals(initialRank) && Rank.MARSHAL.equals(destinationRank)) {
				result = List.of(StrategoMovementResultDTO.builder().rank(Rank.SCOUT).isHost(true).build());
			} else {
				result = List.of();
			}

			Mockito.when(mapper.writeValueAsString(result)).thenReturn("json");
		}

		strategoService.addMovement(GAME_ID, player, movementDto);

		var captor = ArgumentCaptor.forClass(StrategoStatus.class);
		Mockito.verify(strategoStatusRepository).save(captor.capture());

		assertThat(captor.getValue()).isNotNull().satisfies(savedStatus -> {
			var savedBoard = savedStatus.getBoard();
			var init = savedBoard.get(1).get(2);
			var dest = savedBoard.get(3).get(4);

			assertThat(init).isNull();

			if (finalRank == null) {
				assertThat(dest).isNull();
			} else {
				assertThat(dest.getRank()).isEqualTo(finalRank);
			}
		});

		var resultCaptor = ArgumentCaptor.forClass(List.class);

		Mockito.verify(mapper).writeValueAsString(resultCaptor.capture());
		assertThat(resultCaptor.getValue()).isEqualTo(result);
	}

	private ArmySetupDTO getValidSetup() {
		return ArmySetupDTO.builder().army(List.of(//
				Arrays.asList(new Rank[] { Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB,
						Rank.BOMB, Rank.BOMB, Rank.BOMB }), //
				Arrays.asList(new Rank[] { Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB,
						Rank.BOMB, Rank.BOMB, Rank.BOMB }), //
				Arrays.asList(new Rank[] { Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB,
						Rank.BOMB, Rank.BOMB, Rank.BOMB }), //
				Arrays.asList(new Rank[] { Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB,
						Rank.BOMB, Rank.BOMB, Rank.BOMB }) //
		)) //
				.build();
	}

	@Test
	void testAddSetupNoGame() {
		var player = getTestPlayer();
		var setup = getValidSetup();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> strategoService.addSetup(GAME_ID, player, setup))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game does not exist");
	}

	private static class InvalidSetupArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(HOST_ID, true, false), // Same ranks
					Arguments.of(GUEST_ID, false, true)// , // Player wins
			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = InvalidSetupArguments.class)
	void testAddSetupInvalidSetup(Integer playerId, boolean isHostInitialized, boolean isGuestInitialized) {
		var player = getTestPlayer(playerId);
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);

		var game = getTestGame(host, guest);
		var setup = getValidSetup();
		game.setPhase(GamePhase.WAITING_FOR_SETUP_1_PLAYER);

		var statusMock = Mockito.mock(StrategoStatus.class);
		if (isHostInitialized) {
			Mockito.when(statusMock.getIsHostInitialized()).thenReturn(isHostInitialized);
		} else {
			Mockito.when(statusMock.getIsGuestInitialized()).thenReturn(isGuestInitialized);
		}

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.of(statusMock));

		assertThatThrownBy(() -> strategoService.addSetup(GAME_ID, player, setup))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid player setup");

		Mockito.verifyNoMoreInteractions(strategoStatusRepository);
	}

	@Test
	void testAddSetupNotInSetupState() {
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);
		var player = host;

		var game = getTestGame(host, guest);
		game.setPhase(GamePhase.PLAYING);
		var setup = getValidSetup();

		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

		assertThatThrownBy(() -> strategoService.addSetup(GAME_ID, player, setup))
				.isInstanceOf(MatchmakingValidationException.class).hasMessage("Game not in setup state");

		Mockito.verifyNoMoreInteractions(strategoStatusRepository);
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

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void testAddSetupHost(boolean useExistingStatus) {
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);
		var player = host;

		var game = getTestGame(host, guest);
		var setup = getValidSetup();
		game.setPhase(GamePhase.WAITING_FOR_SETUP_1_PLAYER);

		StrategoStatus status = null;
		if (useExistingStatus) {
			status = getTestStatus(getEmptyBoard(), game);
			status.setIsHostInitialized(false);
			status.setIsGuestInitialized(true);
		}

		Mockito.when(strategoStatusRepository.save(Mockito.any(StrategoStatus.class))).thenAnswer(inv -> {
			StrategoStatus statusToSave = inv.getArgument(0);
			statusToSave.setId(STATUS_ID);
			return statusToSave;
		});
		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.ofNullable(status));

		var gameStateDto = strategoService.addSetup(GAME_ID, player, setup);

		var captor = ArgumentCaptor.forClass(StrategoStatus.class);
		var numInvocationsToSave = useExistingStatus ? 1 : 2;
		Mockito.verify(strategoStatusRepository, Mockito.times(numInvocationsToSave)).save(captor.capture());

		assertThat(captor.getAllValues()).hasSize(numInvocationsToSave).anySatisfy(aStatus -> {
			assertThat(aStatus.getId()).isEqualTo(STATUS_ID);
		});

		assertThat(gameStateDto).isNotNull().satisfies(gameState -> {
			checkHostBoard(gameState.getBoard(), setup);
		});
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void testAddSetupGuest(boolean useExistingStatus) {
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);
		var player = guest;

		var game = getTestGame(host, guest);
		var setup = getValidSetup();
		game.setPhase(GamePhase.WAITING_FOR_SETUP_1_PLAYER);

		StrategoStatus status = null;
		if (useExistingStatus) {
			status = getTestStatus(getEmptyBoard(), game);
			status.setIsHostInitialized(true);
			status.setIsGuestInitialized(false);
		}

		Mockito.when(strategoStatusRepository.save(Mockito.any(StrategoStatus.class))).thenAnswer(inv -> {
			StrategoStatus statusToSave = inv.getArgument(0);
			statusToSave.setId(STATUS_ID);
			return statusToSave;
		});
		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.ofNullable(status));

		var gameStateDto = strategoService.addSetup(GAME_ID, player, setup);

		var captor = ArgumentCaptor.forClass(StrategoStatus.class);
		var numInvocationsToSave = useExistingStatus ? 1 : 2;
		Mockito.verify(strategoStatusRepository, Mockito.times(numInvocationsToSave)).save(captor.capture());

		assertThat(captor.getAllValues()).hasSize(numInvocationsToSave).anySatisfy(aStatus -> {
			assertThat(aStatus.getId()).isEqualTo(STATUS_ID);
		});

		assertThat(gameStateDto).isNotNull().satisfies(gameState -> {
			checkGuestBoard(gameState.getBoard(), setup);
		});
	}

	@ParameterizedTest
	@EnumSource(value = GamePhase.class, names = { //
			"WAITING_FOR_SETUP_2_PLAYERS", //
			"WAITING_FOR_SETUP_1_PLAYER", //
	})
	void testAddSetupState(GamePhase gamePhase) {
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);
		var player = guest;

		var game = getTestGame(host, guest);
		var setup = getValidSetup();
		game.setPhase(gamePhase);

		var status = getTestStatus(getEmptyBoard(), game);
		status.setIsHostInitialized(true);
		status.setIsGuestInitialized(false);

		Mockito.when(strategoStatusRepository.save(Mockito.any(StrategoStatus.class))).thenAnswer(inv -> {
			StrategoStatus statusToSave = inv.getArgument(0);
			statusToSave.setId(STATUS_ID);
			return statusToSave;
		});
		Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
		Mockito.when(strategoStatusRepository.findByGameId(GAME_ID)).thenReturn(Optional.ofNullable(status));

		var gameStateDto = strategoService.addSetup(GAME_ID, player, setup);

		var nextPhase = GamePhase.WAITING_FOR_SETUP_1_PLAYER.equals(gamePhase) //
				? GamePhase.PLAYING //
				: GamePhase.WAITING_FOR_SETUP_1_PLAYER;

		var gameCaptor = ArgumentCaptor.forClass(Game.class);
		Mockito.verify(gameRepository).save(gameCaptor.capture());

		assertThat(gameCaptor.getValue()).satisfies(aGame -> {
			assertThat(aGame.getId()).isEqualTo(GAME_ID.intValue());
			assertThat(aGame.getPhase()).isEqualTo(nextPhase);
		});

		var captor = ArgumentCaptor.forClass(StrategoStatus.class);
		Mockito.verify(strategoStatusRepository).save(captor.capture());

		assertThat(captor.getAllValues()).hasSize(1).anySatisfy(aStatus -> {
			assertThat(aStatus.getId()).isEqualTo(STATUS_ID);
			assertThat(aStatus.getGame().getPhase()).isEqualTo(nextPhase);
		});

		assertThat(gameStateDto).isNotNull().satisfies(gameState -> {
			checkGuestBoard(gameState.getBoard(), setup);
		});
	}

	private void checkHostBoard(List<List<BoardTileDTO>> board, ArmySetupDTO setup) {
		var ranks = setup.getArmy();
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 10; col++) {
				assertThat(board.get(row).get(col).getRank()).isEqualTo(ranks.get(row).get(col));
			}
		}
	}

	private void checkGuestBoard(List<List<BoardTileDTO>> board, ArmySetupDTO setup) {
		var ranks = setup.getArmy();
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 10; col++) {
				assertThat(board.get(6 + row).get(col).getRank()).isEqualTo(ranks.get(row).get(col));
			}
		}
	}

	private static StrategoMovement getTestMovement() {
		var host = getTestPlayer(HOST_ID);
		var guest = getTestPlayer(GUEST_ID);
		var movement = new StrategoMovement();
		movement.setId(1);
		movement.setIsGuestTurn(true);
		movement.setGame(getTestGame(host, guest));
		movement.setRank(Rank.BOMB);
		movement.setRowInitial(1);
		movement.setColInitial(2);
		movement.setRowFinal(3);
		movement.setColFinal(4);
		movement.setResult("[{\"rank\":\"MARSHAL\",\"isHost\":true}]");
		return movement;
	}

	private StrategoMovementDTO getTestMovementDto() {
		return StrategoMovementDTO.builder() //
				.rank(Rank.BOMB) //
				.rowInitial(1) //
				.colInitial(2) //
				.rowFinal(3) //
				.colFinal(4) //
				.build();
	}

	private static Game getTestGame(Player host, Player guest) {
		var game = new Game();
		game.setId(GAME_ID.intValue());
		game.setCreationDate(Instant.now());
		game.setHost(host);
		game.setGuest(guest);
		game.setPhase(GamePhase.PLAYING);
		return game;
	}

	private static Player getTestPlayer() {
		return getTestPlayer(PLAYER_ID);
	}

	private static Player getTestPlayer(Integer playerId) {
		var player = new Player();
		player.setId(playerId);
		player.setUserName(PLAYER_USERNAME);
		return player;
	}

	private List<List<BoardTileDTO>> getBoard(BoardTileDTO playerTile, int row, int col) {
		var tile = new BoardTileDTO(Rank.DISABLED, true);

		var row0 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row1 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row2 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row3 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row4 = new BoardTileDTO[] { null, null, tile, tile, null, null, tile, tile, null, null };
		var row5 = new BoardTileDTO[] { null, null, tile, tile, null, null, tile, tile, null, null };
		var row6 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row7 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row8 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };
		var row9 = new BoardTileDTO[] { null, null, null, null, null, null, null, null, null, null };

		var finalRow = switch (row) {
		case 0 -> row0;
		case 1 -> row1;
		case 2 -> row2;
		case 3 -> row3;
		case 4 -> row4;
		case 5 -> row5;
		case 6 -> row6;
		case 7 -> row7;
		case 8 -> row8;
		case 9 -> row9;
		default -> row0;
		};

		finalRow[col] = playerTile;

		List<List<BoardTileDTO>> board = List.of(//
				Arrays.asList(row0), //
				Arrays.asList(row1), //
				Arrays.asList(row2), //
				Arrays.asList(row3), //
				Arrays.asList(row4), //
				Arrays.asList(row5), //
				Arrays.asList(row6), //
				Arrays.asList(row7), //
				Arrays.asList(row8), //
				Arrays.asList(row9) //
		);

		return board;

	}

	private boolean isDisabledSquare(int row, int col) {
		return (4 <= row && row <= 5) && (2 <= col && col <= 3 || 6 <= col && col <= 7);
	}

	@ParameterizedTest
	@ValueSource(ints = { //
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, //
			20, 21, 22, 23, 24, 25, 26, 27, 28, 29, //
			30, 31, 32, 33, 34, 35, 36, 37, 38, 39, //
			40, 41, 42, 43, 44, 45, 46, 47, 48, 49, //
			50, 51, 52, 53, 54, 55, 56, 57, 58, 59, //
			60, 61, 62, 63, 64, 65, 66, 67, 68, 69, //
			70, 71, 72, 73, 74, 75, 76, 77, 78, 79, //
			80, 81, 82, 83, 84, 85, 86, 87, 88, 89, //
			90, 91, 92, 93, 94, 95, 96, 97, 98, 99 //
	})
	void testAddMovementAllSquares(int squareId) {
		var row = squareId / 10;
		var col = squareId % 10;

		if (!isDisabledSquare(row, col)) {
			for (var direction = 0; direction < 4; direction++) {
				int rowTarget = -1;
				int colTarget = -1;

				switch (direction) {
				case 0:
					rowTarget = row - 1;
					colTarget = col;
					break;
				case 1:
					rowTarget = row + 1;
					colTarget = col;
					break;
				case 2:
					rowTarget = row;
					colTarget = col - 1;
					break;
				case 3:
				default:
					rowTarget = row;
					colTarget = col + 1;
					break;
				}

				if (0 <= rowTarget && rowTarget < 10 && 0 <= colTarget && colTarget < 10
						&& !isDisabledSquare(rowTarget, colTarget)) {

					Mockito.reset(gameRepository, strategoStatusRepository, strategoMovementRepository);

					var host = getTestPlayer(HOST_ID);
					var guest = getTestPlayer(GUEST_ID);
					var game = getTestGame(host, guest);
					Mockito.when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));

					var playerTile = new BoardTileDTO(Rank.MARSHAL, false);
					var board = getBoard(playerTile, row, col);

					Mockito.when(strategoStatusRepository.findByGameId(GAME_ID))
							.thenReturn(Optional.of(getTestStatus(board, game)));

					var movement = StrategoMovementDTO.builder() //
							.rank(Rank.MARSHAL) //
							.rowInitial(row) //
							.colInitial(col) //
							.rowFinal(rowTarget) //
							.colFinal(colTarget) //
							.build();

					strategoService.addMovement(GAME_ID, guest, movement);

					var captor = ArgumentCaptor.forClass(StrategoStatus.class);
					Mockito.verify(strategoStatusRepository).save(captor.capture());
					StrategoStatus status = captor.getValue();
					assertThat(status.getIsGuestTurn()).isFalse();

					var captorMove = ArgumentCaptor.forClass(StrategoMovement.class);
					Mockito.verify(strategoMovementRepository).save(captorMove.capture());
					assertThat(captorMove.getValue().getRowFinal()).isEqualTo(rowTarget);
					assertThat(captorMove.getValue().getColFinal()).isEqualTo(colTarget);
					assertThat(captorMove.getValue().getIsGuestTurn()).isTrue();
				}
			}
		}
	}
}
