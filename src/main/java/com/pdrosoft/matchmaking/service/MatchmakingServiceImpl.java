package com.pdrosoft.matchmaking.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pdrosoft.matchmaking.dto.GameDTO;
import com.pdrosoft.matchmaking.dto.GameExtendedDTO;
import com.pdrosoft.matchmaking.dto.GameInputDTO;
import com.pdrosoft.matchmaking.dto.PlayerDTO;
import com.pdrosoft.matchmaking.exception.PlayerExistsException;
import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.repository.PlayerRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Service
public class MatchmakingServiceImpl implements MatchmakingService {

	@NonNull
	private final GameService gameService;
	@NonNull
	private final PlayerRepository playerRepository;
	@NonNull
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public List<GameDTO> getGameList(Instant dateFrom) {
		return gameService.getGameList(dateFrom);
	}

	private PlayerDTO toPlayerDTO(Player player) {
		return PlayerDTO.builder().id(player.getId()).username(player.getUserName()).build();

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PlayerDTO addPlayer(String name, String password) {
		var playerOpt = playerRepository.findPlayersByName(name);

		if (playerOpt.isPresent()) {
			throw new PlayerExistsException("player already exists '%s'".formatted(name));
		}

		var player = new Player();
		player.setUserName(name);
		player.setPassword(passwordEncoder.encode(password));

		return Optional.ofNullable(playerRepository.save(player)).map(this::toPlayerDTO).orElseThrow();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public GameDTO addGame(Player host, GameInputDTO gameInputDto) {
		return gameService.addGame(host, gameInputDto);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public GameExtendedDTO joinGame(Player guest, Long gameId) {
		return gameService.joinGame(guest, gameId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public GameDTO leaveGame(Player player, Long gameId) {
		return gameService.leaveGame(player, gameId);
	}

	@Override
	public GameExtendedDTO getGame(Player player, Long gameId) {
		return gameService.getGame(player, gameId);
	}

}
