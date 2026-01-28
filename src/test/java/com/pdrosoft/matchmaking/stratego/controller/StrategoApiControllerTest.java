package com.pdrosoft.matchmaking.stratego.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdrosoft.matchmaking.dto.ErrorResultDTO;
import com.pdrosoft.matchmaking.dto.LoginResultDTO;
import com.pdrosoft.matchmaking.dto.UserAuthDTO;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.BoardTileDTO;
import com.pdrosoft.matchmaking.stratego.dto.GameStateDTO;
import com.pdrosoft.matchmaking.stratego.dto.StrategoMovementDTO;
import com.pdrosoft.matchmaking.stratego.enums.GamePhase;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:test-gameplay-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class StrategoApiControllerTest {

	private static final Long GAME_ID = 5L;

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper = null;

	private ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
			// support Java 8 date time apis
			mapper.registerModule(new JavaTimeModule());
		}

		return mapper;
	}

	private String getToken(String user, String password) throws Exception {
		var authData = UserAuthDTO.builder().username(user).password(password).build();

		var json = getObjectMapper().writeValueAsString(authData);
		var result = mockMvc.perform(put("/api/auth/login")//
				.contentType(MediaType.APPLICATION_JSON)//
				.content(json))//
				.andExpect(status().isOk()).andReturn();

		var authDTO = getObjectMapper().readValue(result.getResponse().getContentAsString(), LoginResultDTO.class);

		return authDTO.getToken();
	}

	@Test
	void testAddSetupInvalidSetup() throws Exception {

		var setupDto = ArmySetupDTO.builder().army(List.of()).build();
		var json = getObjectMapper().writeValueAsString(setupDto);

		var token1 = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/%d/setup".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isBadRequest()).andReturn();

		var resultDto = getObjectMapper().readValue(result.getResponse().getContentAsString(), ErrorResultDTO.class);
		assertThat(resultDto.getMessage()).contains("'army': rejected value").contains("Invalid army setup");
	}

	List<List<Rank>> getValidSetup() {
		return List.of(
				List.of(Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.BOMB, Rank.SPY, Rank.FLAG,
						Rank.MARSHAL, Rank.GENERAL),
				List.of(Rank.COLONEL, Rank.COLONEL, Rank.MAJOR, Rank.MAJOR, Rank.MAJOR, Rank.CAPTAIN, Rank.CAPTAIN,
						Rank.CAPTAIN, Rank.CAPTAIN, Rank.LIEUTENANT),
				List.of(Rank.LIEUTENANT, Rank.LIEUTENANT, Rank.LIEUTENANT, Rank.SERGEANT, Rank.SERGEANT, Rank.SERGEANT,
						Rank.SERGEANT, Rank.MINER, Rank.MINER, Rank.MINER),
				List.of(Rank.MINER, Rank.MINER, Rank.SCOUT, Rank.SCOUT, Rank.SCOUT, Rank.SCOUT, Rank.SCOUT, Rank.SCOUT,
						Rank.SCOUT, Rank.SCOUT));
	}

	@Test
	void testAddSetupSuccess() throws Exception {

		var board = getValidSetup();
		var setupDto = ArmySetupDTO.builder().army(board).build();
		var json = getObjectMapper().writeValueAsString(setupDto);

		// Host
		var token1 = getToken("testuser1", "password1");
		var resultHost = mockMvc.perform(put("/api/stratego/%d/setup".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		GameStateDTO gameState = getObjectMapper().readValue(resultHost.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});
		assertThat(gameState).isNotNull();
		assertThat(gameState.getGameId()).isEqualTo(GAME_ID);
		assertThat(gameState.getMovement()).isNull();
		assertThat(gameState.getPhase()).isEqualTo(GamePhase.WAITING_FOR_SETUP_1_PLAYER);

		checkHostBoard(gameState.getBoard(), setupDto);

		// Guest
		var token2 = getToken("testuser2", "password2");
		var resultGuest = mockMvc.perform(put("/api/stratego/%d/setup".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token2)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		GameStateDTO gameState2 = getObjectMapper().readValue(resultGuest.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});
		assertThat(gameState2).isNotNull();
		assertThat(gameState2.getGameId()).isEqualTo(GAME_ID);
		assertThat(gameState2.getMovement()).isNull();
		assertThat(gameState2.getPhase()).isEqualTo(GamePhase.PLAYING);

		checkHostBoard(gameState2.getBoard(), setupDto);
		checkGuestBoard(gameState2.getBoard(), setupDto);
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
				assertThat(board.get(9 - row).get(9 - col).getRank()).isEqualTo(ranks.get(row).get(col));
			}
		}
	}

	private List<List<BoardTileDTO>> initializeGame() throws Exception {
		var board = getValidSetup();
		var setupDto = ArmySetupDTO.builder().army(board).build();
		var json = getObjectMapper().writeValueAsString(setupDto);

		// Host
		var token1 = getToken("testuser1", "password1");
		mockMvc.perform(put("/api/stratego/%d/setup".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		// Guest
		var token2 = getToken("testuser2", "password2");
		var resultGuest = mockMvc.perform(put("/api/stratego/%d/setup".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token2)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();
		GameStateDTO gameState2 = getObjectMapper().readValue(resultGuest.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});

		return gameState2.getBoard();
	}

	@Test
	void testAddMovementInvalidDetination() throws Exception {

		initializeGame();
		var movementDto = StrategoMovementDTO.builder() //
				.rowInitial(3) //
				.colInitial(7) //
				.rowFinal(4) //
				.colFinal(7) //
				.rank(Rank.SPY) //
				.build();
		var json = getObjectMapper().writeValueAsString(movementDto);

		// Host
		var token1 = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/%d/movement".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isBadRequest()).andReturn();

		var resultDto = getObjectMapper().readValue(result.getResponse().getContentAsString(), ErrorResultDTO.class);
		assertThat(resultDto.getMessage()).contains("Invalid destination square");

	}

	@Test
	void testAddMovementSuccess() throws Exception {

		initializeGame();
		var movementDto = StrategoMovementDTO.builder() //
				.rowInitial(3) //
				.colInitial(9) //
				.rowFinal(4) //
				.colFinal(9) //
				.rank(Rank.SCOUT) //
				.build();
		var json = getObjectMapper().writeValueAsString(movementDto);

		// Host
		var token1 = getToken("testuser1", "password1");
		var resultHost = mockMvc.perform(put("/api/stratego/%d/movement".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		GameStateDTO gameState = getObjectMapper().readValue(resultHost.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});
		assertThat(gameState).isNotNull();
		assertThat(gameState.getGameId()).isEqualTo(GAME_ID);
		assertThat(gameState.getMovement()).isNotNull().satisfies(movement -> {
			assertThat(movement.getRank()).isEqualTo(movementDto.getRank());
			assertThat(movement.getRowInitial()).isEqualTo(movementDto.getRowInitial());
			assertThat(movement.getRowFinal()).isEqualTo(movementDto.getRowFinal());
			assertThat(movement.getColInitial()).isEqualTo(movementDto.getColInitial());
			assertThat(movement.getColFinal()).isEqualTo(movementDto.getColFinal());
		});
		assertThat(gameState.getPhase()).isEqualTo(GamePhase.PLAYING);
		assertThat(gameState.getBoard().get(movementDto.getRowFinal()).get(movementDto.getColFinal())).isNotNull()
				.satisfies(tile -> {
					assertThat(tile.getRank()).isEqualTo(Rank.SCOUT);
					assertThat(tile.isHostOwner()).isTrue();
				});
		assertThat(gameState.getBoard().get(movementDto.getRowInitial()).get(movementDto.getColInitial())).isNull();
		assertThat(gameState.isMyTurn()).isFalse();

		// Guest
		var movementDtoGuest = StrategoMovementDTO.builder() //
				.rowInitial(6) //
				.colInitial(9) //
				.rowFinal(5) //
				.colFinal(9) //
				.rank(Rank.SCOUT) //
				.build();
		var jsonGuest = getObjectMapper().writeValueAsString(movementDtoGuest);

		var token2 = getToken("testuser2", "password2");
		var resultGuest = mockMvc.perform(put("/api/stratego/%d/movement".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token2)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(jsonGuest) //
		).andExpect(status().isOk()).andReturn();

		GameStateDTO gameStateGuest = getObjectMapper().readValue(resultGuest.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});
		assertThat(gameStateGuest).isNotNull();
		assertThat(gameStateGuest.getGameId()).isEqualTo(GAME_ID);
		assertThat(gameStateGuest.getMovement()).isNotNull().satisfies(movement -> {
			assertThat(movement.getRank()).isEqualTo(movementDtoGuest.getRank());
			assertThat(movement.getRowInitial()).isEqualTo(movementDtoGuest.getRowInitial());
			assertThat(movement.getRowFinal()).isEqualTo(movementDtoGuest.getRowFinal());
			assertThat(movement.getColInitial()).isEqualTo(movementDtoGuest.getColInitial());
			assertThat(movement.getColFinal()).isEqualTo(movementDtoGuest.getColFinal());
		});
		assertThat(gameStateGuest.getPhase()).isEqualTo(GamePhase.PLAYING);
		assertThat(gameStateGuest.getBoard().get(movementDtoGuest.getRowFinal()).get(movementDtoGuest.getColFinal()))
				.isNotNull().satisfies(tile -> {
					assertThat(tile.getRank()).isEqualTo(Rank.MINER);
					assertThat(tile.isHostOwner()).isFalse();
				});
		assertThat(
				gameStateGuest.getBoard().get(movementDtoGuest.getRowInitial()).get(movementDtoGuest.getColInitial()))
				.isNull();
		assertThat(gameStateGuest.isMyTurn()).isFalse();
	}

	@Test
	void testGetStatus() throws Exception {

		initializeGame();
		var movementDto = StrategoMovementDTO.builder() //
				.rowInitial(3) //
				.colInitial(9) //
				.rowFinal(4) //
				.colFinal(9) //
				.rank(Rank.SCOUT) //
				.build();
		var json = getObjectMapper().writeValueAsString(movementDto);

		// Host
		var token1 = getToken("testuser1", "password1");
		mockMvc.perform(put("/api/stratego/%d/movement".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		var resultStatus = mockMvc.perform(get("/api/stratego/%d/status".formatted(GAME_ID)) //
				.header("Authorization", "Bearer %s".formatted(token1)) //
		).andExpect(status().isOk()).andReturn();

		GameStateDTO gameState = getObjectMapper().readValue(resultStatus.getResponse().getContentAsString(),
				new TypeReference<GameStateDTO>() {
				});
		assertThat(gameState).isNotNull();
		assertThat(gameState.getGameId()).isEqualTo(GAME_ID);
		assertThat(gameState.getMovement()).isNotNull().satisfies(movement -> {
			assertThat(movement.getRank()).isEqualTo(movementDto.getRank());
			assertThat(movement.getRowInitial()).isEqualTo(movementDto.getRowInitial());
			assertThat(movement.getRowFinal()).isEqualTo(movementDto.getRowFinal());
			assertThat(movement.getColInitial()).isEqualTo(movementDto.getColInitial());
			assertThat(movement.getColFinal()).isEqualTo(movementDto.getColFinal());
		});
		assertThat(gameState.getPhase()).isEqualTo(GamePhase.PLAYING);
		assertThat(gameState.getBoard().get(movementDto.getRowFinal()).get(movementDto.getColFinal())).isNotNull()
				.satisfies(tile -> {
					assertThat(tile.getRank()).isEqualTo(Rank.SCOUT);
					assertThat(tile.isHostOwner()).isTrue();
				});
		assertThat(gameState.getBoard().get(movementDto.getRowInitial()).get(movementDto.getColInitial())).isNull();
		assertThat(gameState.isMyTurn()).isFalse();

	}
}
