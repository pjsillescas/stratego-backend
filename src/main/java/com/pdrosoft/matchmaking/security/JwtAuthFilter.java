package com.pdrosoft.matchmaking.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pdrosoft.matchmaking.service.MatchmakingUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final MatchmakingUserDetailsService matchmakingUserDetailsService;

	public JwtAuthFilter(JwtUtil jwtUtil, @Autowired MatchmakingUserDetailsService matchmakingUserDetailsService) {
		this.jwtUtil = jwtUtil;
		this.matchmakingUserDetailsService = matchmakingUserDetailsService;
	}

	private boolean isSignupRequest(HttpServletRequest request) {
		var path = request.getServletPath();
		return path != null && path.contains("/auth/signup");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ") && !isSignupRequest(request)) {
			String token = authHeader.substring(7);
			if (jwtUtil.isTokenValid(token)) {
				String username = jwtUtil.extractUsername(token);

				// var user = new User(username, "", Collections.emptyList());
				var user = matchmakingUserDetailsService.loadUserByUsername(username);
				var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}

		filterChain.doFilter(request, response);
	}
}
