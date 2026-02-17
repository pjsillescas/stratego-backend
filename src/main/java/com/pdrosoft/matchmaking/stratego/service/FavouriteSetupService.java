package com.pdrosoft.matchmaking.stratego.service;

import java.util.List;
import java.util.Optional;

import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.stratego.dto.FavouriteSetupDTO;

public interface FavouriteSetupService {

	Optional<FavouriteSetupDTO> addSetup(FavouriteSetupDTO favouriteSetupDTO, Player player);

	Optional<FavouriteSetupDTO> updateSetup(Integer setupId, FavouriteSetupDTO favouriteSetupDto, Player player);

	Optional<FavouriteSetupDTO> deleteSetup(Integer setupId, Player player);

	List<FavouriteSetupDTO> getSetupList(Player player);

}
