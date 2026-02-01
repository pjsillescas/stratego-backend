package com.pdrosoft.matchmaking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pdrosoft.matchmaking.dao.PlayerDAO;
import com.pdrosoft.matchmaking.model.Player;
import com.pdrosoft.matchmaking.security.payload.MatchmakingUserDetails;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Service
public class MatchmakingUserDetailsServiceImpl implements MatchmakingUserDetailsService {

	@NonNull
	private final PlayerDAO playerDao;

	private UserDetails toUserDetails(Player player) {
		return MatchmakingUserDetails.builder().userName(player.getUserName()) //
				//.password("{bcrypt}%s".formatted(player.getPassword())) // {noop} = no encoding
				.password(player.getPassword()) //
				.authorities(List.of(new SimpleGrantedAuthority("USER"))) //
				.player(player) //
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return playerDao.findPlayersByName(username).map(this::toUserDetails)
				.orElseThrow(() -> new UsernameNotFoundException("Not found user '%s'".formatted(username)));
	}

}
