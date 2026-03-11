package com.pdrosoft.matchmaking.chat.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.pdrosoft.matchmaking.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_HEADER_PREFIX = "Bearer ";
	private static final String TOKEN_QUERY_STRING = "token";

	@NonNull
	private final JwtUtil jwtUtil;

	private Map<String, String> parseQueryString(String query) {
		var map = new HashMap<String, String>();
		Optional.ofNullable(query).map(str -> List.of(str.split("&"))).orElse(List.of()) //
				.forEach(param -> Optional.ofNullable(param).map(par -> par.split("="))
						.ifPresent(pair -> map.put(pair[0], pair.length == 2 ? pair[1] : null)));
		return map;
	}

	private Optional<String> extractTokenFromHeader(HttpServletRequest req) {
		return Optional.ofNullable(req.getHeader(AUTHORIZATION_HEADER)) //
				.filter(header -> header.startsWith(BEARER_HEADER_PREFIX)) //
				.map(header -> header.substring(BEARER_HEADER_PREFIX.length()));
	}

	private Optional<String> extractTokenFromQueryString(HttpServletRequest req) {
		return Optional.ofNullable(req.getQueryString()).map(this::parseQueryString)
				.map(map -> map.getOrDefault(TOKEN_QUERY_STRING, null));
	}

	private Optional<String> extractToken(HttpServletRequest req) {
		return extractTokenFromHeader(req) //
				.or(() -> extractTokenFromQueryString(req)) //
		;
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		if (request instanceof ServletServerHttpRequest servletRequest) {

			HttpServletRequest req = servletRequest.getServletRequest();

			return extractToken(req) // Get token

					.filter(jwtUtil::isTokenValid).map(token -> {
						attributes.put("username", jwtUtil.extractUsername(token));

						return true;
					}).orElse(false);
		}

		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			@Nullable Exception exception) {
	}
}