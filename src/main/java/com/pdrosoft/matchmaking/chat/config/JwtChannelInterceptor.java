package com.pdrosoft.matchmaking.chat.config;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.pdrosoft.matchmaking.security.JwtUtil;
import com.pdrosoft.matchmaking.service.GameService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class JwtChannelInterceptor implements ChannelInterceptor {

	@NonNull
	private final JwtUtil jwtUtil;
	
	@NonNull
	private final GameService gameService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);

				if (jwtUtil.isTokenValid(token)) {
					String username = jwtUtil.extractUsername(token);
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
							List.of());

					accessor.setUser(auth);
				}
			}

		}

		/*
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            String username = accessor.getUser().getName();

            Long gameId = extractGameId(destination);

            if (!gameService.isPlayerInGame(username, gameId)) {
                throw new AccessDeniedException("Not your game");
            }
        }
        */

        return message;
	}
}