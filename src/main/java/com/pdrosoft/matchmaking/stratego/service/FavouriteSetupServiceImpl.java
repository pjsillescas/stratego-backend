package com.pdrosoft.matchmaking.stratego.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdrosoft.matchmaking.model.FavouriteSetup;
import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.repository.FavouriteSetupRepository;
import com.pdrosoft.matchmaking.stratego.dto.ArmySetupDTO;
import com.pdrosoft.matchmaking.stratego.dto.FavouriteSetupDTO;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Service
public class FavouriteSetupServiceImpl implements FavouriteSetupService {

	@NonNull
	FavouriteSetupRepository favouriteSetupRepository;

	@NonNull
	private final ObjectMapper mapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Optional<FavouriteSetupDTO> addSetup(FavouriteSetupDTO favouriteSetupDTO, Player player) {
		var setup = new FavouriteSetup();
		updateEntity(favouriteSetupDTO, setup, player);

		return Optional.ofNullable(favouriteSetupRepository.save(setup)).map(this::parse);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Optional<FavouriteSetupDTO> updateSetup(Integer setupId, FavouriteSetupDTO favouriteSetupDto,
			Player player) {
		var setupOpt = favouriteSetupRepository.findById(setupId);

		if (setupOpt.isPresent()) {
			var setup = setupOpt.get();
			updateEntity(favouriteSetupDto, setup, player);

			return Optional.ofNullable(favouriteSetupRepository.save(setup)).map(this::parse);
		}

		return Optional.empty();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Optional<FavouriteSetupDTO> deleteSetup(Integer setupId, Player player) {
		var setupOpt = favouriteSetupRepository.findById(setupId);
		if (setupOpt.isPresent()) {
			var setup = setupOpt.get();
			var setupDto = parse(setup);
			favouriteSetupRepository.delete(setup);

			return Optional.of(setupDto);
		}
		return Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public List<FavouriteSetupDTO> getSetupList(Player player) {
		return favouriteSetupRepository.getSetupList(player).stream().map(this::parse).toList();
	}

	private void updateEntity(FavouriteSetupDTO favouriteSetupDto, FavouriteSetup setup, Player owner) {
		String json;

		try {
			json = mapper.writeValueAsString(favouriteSetupDto.getArmySetupDTO().getArmy());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			json = "";
		}
		setup.setDescription(favouriteSetupDto.getDescription());
		setup.setOwner(owner);
		setup.setSetupJson(json);
	}

	private FavouriteSetupDTO parse(FavouriteSetup setup) {
		List<List<Rank>> army;
		try {
			army = mapper.readValue(setup.getSetupJson(), new TypeReference<List<List<Rank>>>() {
			});
		} catch (JsonProcessingException e) {
			army = null;
		}

		return FavouriteSetupDTO.builder() //
				.id(setup.getId()) //
				.armySetupDTO(ArmySetupDTO.builder() //
						.army(army) //
						.build()) //
				.description(setup.getDescription()) //
				.build();
	}
}
