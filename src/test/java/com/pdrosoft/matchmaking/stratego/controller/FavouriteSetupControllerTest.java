package com.pdrosoft.matchmaking.stratego.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import com.pdrosoft.matchmaking.dto.ErrorResultDTO;
import com.pdrosoft.matchmaking.dto.LoginResultDTO;
import com.pdrosoft.matchmaking.dto.UserAuthDTO;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.FavouriteSetupDTO;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:test-favourite-setup-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class FavouriteSetupControllerTest {

	private static final Integer NEW_ID = 5;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	private String getToken(String user, String password) throws Exception {
		var authData = UserAuthDTO.builder().username(user).password(password).build();

		var json = mapper.writeValueAsString(authData);
		var result = mockMvc.perform(put("/api/auth/login")//
				.contentType(MediaType.APPLICATION_JSON)//
				.content(json))//
				.andExpect(status().isOk()).andReturn();

		var authDTO = mapper.readValue(result.getResponse().getContentAsString(), LoginResultDTO.class);

		return authDTO.getToken();
	}

	@Test
	void testSetupListSuccess() throws Exception {
		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(get("/api/stratego/favourite/setup") //
				.header("Authorization", "Bearer %s".formatted(token)))//
				.andExpect(status().isOk()).andReturn();

		List<FavouriteSetupDTO> setupList = mapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<List<FavouriteSetupDTO>>() {
				});
		assertThat(setupList).hasSize(2).anySatisfy(setup -> {
			assertThat(setup.getId()).isEqualTo(1);
			assertThat(setup.getDescription()).isEqualTo("setup1");
			assertThat(setup.getArmySetupDTO()).isNotNull();
			assertThat(setup.getArmySetupDTO().getArmy()).hasSize(1).allSatisfy(row -> {
				assertThat(row).containsExactly(Rank.MARSHAL, Rank.FLAG);
			});
		}).anySatisfy(setup -> {
			assertThat(setup.getId()).isEqualTo(3);
			assertThat(setup.getDescription()).isEqualTo("setup3");
			assertThat(setup.getArmySetupDTO()).isNotNull();
			assertThat(setup.getArmySetupDTO().getArmy()).isEmpty();
		});
	}

	@Test
	void testSetupGetSetupSuccess() throws Exception {
		var setupId = 1;
		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)))//
				.andExpect(status().isOk()).andReturn();

		FavouriteSetupDTO setup = mapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<FavouriteSetupDTO>() {
				});
		assertThat(setup.getId()).isEqualTo(1);
		assertThat(setup.getDescription()).isEqualTo("setup1");
		assertThat(setup.getArmySetupDTO()).isNotNull();
		assertThat(setup.getArmySetupDTO().getArmy()).hasSize(1).allSatisfy(row -> {
			assertThat(row).containsExactly(Rank.MARSHAL, Rank.FLAG);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 1500 })
	void testSetupGetNotOwningSetup(Integer setupId) throws Exception {
		var token = getToken("testuser2", "password2");
		var result = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)))//
				.andExpect(status().isOk()).andReturn();

		FavouriteSetupDTO setup = mapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<FavouriteSetupDTO>() {
				});
		assertThat(setup).isNull();
	}

	@Test
	void testAddSetupInvalidSetup() throws Exception {

		var setupDto = FavouriteSetupDTO.builder().description("desc")
				.armySetupDTO(ArmySetupDTO.builder().army(List.of()).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/favourite/setup") //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isBadRequest()).andReturn();

		var resultDto = mapper.readValue(result.getResponse().getContentAsString(), ErrorResultDTO.class);
		assertThat(resultDto.getMessage()).contains("'armySetupDTO.army': rejected value")
				.contains("Invalid army setup");
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "" })
	void testAddSetupNoDescriptionOrArmy(String description) throws Exception {

		var setupDto = FavouriteSetupDTO.builder().description(description)
				.armySetupDTO(ArmySetupDTO.builder().army(List.of()).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/favourite/setup") //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isBadRequest()).andReturn();

		var resultDto = mapper.readValue(result.getResponse().getContentAsString(), ErrorResultDTO.class);
		assertThat(resultDto.getMessage()).contains("'description': rejected value").contains("must not be empty")
				.contains("'armySetupDTO.army': rejected value").contains("Invalid army setup");
	}

	@Test
	void testAddSetupSuccess() throws Exception {

		var setup = getValidSetup();
		var setupDto = FavouriteSetupDTO.builder().description("success add")
				.armySetupDTO(ArmySetupDTO.builder().army(setup).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/favourite/setup") //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		var resultDto = mapper.readValue(result.getResponse().getContentAsString(), FavouriteSetupDTO.class);
		assertThat(resultDto.getId()).isEqualTo(NEW_ID);
		assertThat(resultDto.getDescription()).isEqualTo("success add");
		assertThat(resultDto.getArmySetupDTO().getArmy()).isEqualTo(setup);

		var resultGet = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(NEW_ID)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
		).andExpect(status().isOk()).andReturn();

		var resultGetDto = mapper.readValue(resultGet.getResponse().getContentAsString(), FavouriteSetupDTO.class);
		assertThat(resultGetDto.getId()).isEqualTo(NEW_ID);
		assertThat(resultGetDto.getDescription()).isEqualTo("success add");
		assertThat(resultGetDto.getArmySetupDTO().getArmy()).isEqualTo(setup);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "" })
	void testUpdateSetupNoDescriptionOrArmy(String description) throws Exception {

		var setupDto = FavouriteSetupDTO.builder().description(description)
				.armySetupDTO(ArmySetupDTO.builder().army(List.of()).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");
		var result = mockMvc.perform(put("/api/stratego/favourite/setup/{setupId}", Integer.toString(1)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isBadRequest()).andReturn();

		var resultDto = mapper.readValue(result.getResponse().getContentAsString(), ErrorResultDTO.class);
		assertThat(resultDto.getMessage()).contains("'description': rejected value").contains("must not be empty")
				.contains("'armySetupDTO.army': rejected value").contains("Invalid army setup");
	}

	@Test
	void testUpdateSetupSuccess() throws Exception {

		var setupId = 3;
		var setup = getValidSetup();
		var setupDto = FavouriteSetupDTO.builder().description("success add")
				.armySetupDTO(ArmySetupDTO.builder().army(setup).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");

		var resultGetInit = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
		).andExpect(status().isOk()).andReturn();

		var resultGetInitDto = mapper.readValue(resultGetInit.getResponse().getContentAsString(),
				FavouriteSetupDTO.class);
		assertThat(resultGetInitDto.getId()).isEqualTo(setupId);
		assertThat(resultGetInitDto.getDescription()).isEqualTo("setup3");
		assertThat(resultGetInitDto.getArmySetupDTO().getArmy()).isEmpty();

		var result = mockMvc.perform(put("/api/stratego/favourite/setup/{setup}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		var resultDto = mapper.readValue(result.getResponse().getContentAsString(), FavouriteSetupDTO.class);
		assertThat(resultDto.getId()).isEqualTo(setupId);
		assertThat(resultDto.getDescription()).isEqualTo("success add");
		assertThat(resultDto.getArmySetupDTO().getArmy()).isEqualTo(setup);

		var resultGet = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
		).andExpect(status().isOk()).andReturn();

		var resultGetDto = mapper.readValue(resultGet.getResponse().getContentAsString(), FavouriteSetupDTO.class);
		assertThat(resultGetDto.getId()).isEqualTo(setupId);
		assertThat(resultGetDto.getDescription()).isEqualTo("success add");
		assertThat(resultGetDto.getArmySetupDTO().getArmy()).isEqualTo(setup);
	}

	@ParameterizedTest
	@ValueSource(ints = { //
			1, // correct
			2, // not owner
			7, // does not exit
	})
	void testDeleteSetup(Integer setupId) throws Exception {

		var setup = getValidSetup();
		var setupDto = FavouriteSetupDTO.builder().description("success add")
				.armySetupDTO(ArmySetupDTO.builder().army(setup).build()).build();
		var json = mapper.writeValueAsString(setupDto);

		var token = getToken("testuser1", "password1");

		var resultGetInit = mockMvc.perform(get("/api/stratego/favourite/setup/{setupId}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
		).andExpect(status().isOk()).andReturn();

		var resultGetInitDto = mapper.readValue(resultGetInit.getResponse().getContentAsString(),
				FavouriteSetupDTO.class);
		if (setupId == 1) {
			assertThat(resultGetInitDto.getId()).isEqualTo(setupId);
		} else {
			assertThat(resultGetInitDto).isNull();
		}

		var result = mockMvc.perform(delete("/api/stratego/favourite/setup/{setup}", Integer.toString(setupId)) //
				.header("Authorization", "Bearer %s".formatted(token)) //
				.contentType(MediaType.APPLICATION_JSON) //
				.content(json) //
		).andExpect(status().isOk()).andReturn();

		if (setupId == 1) {
			var resultDto = mapper.readValue(result.getResponse().getContentAsString(), FavouriteSetupDTO.class);
			assertThat(resultDto.getId()).isEqualTo(setupId);
		} else {
			var resultDto = mapper.readValue(result.getResponse().getContentAsString(), FavouriteSetupDTO.class);
			assertThat(resultDto).isNull();
		}
	}

	private List<List<Rank>> getValidSetup() {
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

}
