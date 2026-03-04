package com.pdrosoft.matchmaking.chat.config;

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

	@NonNull
	private final JwtUtil jwtUtil;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		if (request instanceof ServletServerHttpRequest servletRequest) {

			HttpServletRequest req = servletRequest.getServletRequest();

			return Optional.ofNullable(req.getHeader(AUTHORIZATION_HEADER)) //
					.filter(header -> header.startsWith(BEARER_HEADER_PREFIX)) //
					.map(header -> header.substring(BEARER_HEADER_PREFIX.length())) // Get token

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