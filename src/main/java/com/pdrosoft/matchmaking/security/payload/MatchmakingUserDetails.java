package com.pdrosoft.matchmaking.security.payload;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.pdrosoft.matchmaking.model.Player;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchmakingUserDetails implements UserDetails {

	private static final long serialVersionUID = 8351034926591120653L;

	private String userName;
	private String password;
	private Player player;
	private List<GrantedAuthority> authorities;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return userName;
	}

}
