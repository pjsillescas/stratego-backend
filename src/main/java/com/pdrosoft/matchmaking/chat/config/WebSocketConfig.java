package com.pdrosoft.matchmaking.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class WebSocketConfig implements WebSocketConfigurer {

	@NonNull
	private final GameWebSocketHandler gameHandler;
	@NonNull
	private final GameNotificationWebSocketHandler notificationHandler;
	@NonNull
	private final JwtHandshakeInterceptor jwtInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(gameHandler, "/ws") //
				.addInterceptors(jwtInterceptor) //
				.setAllowedOrigins("*");
		registry.addHandler(notificationHandler, "/wsn") //
				.addInterceptors(jwtInterceptor) //
				.setAllowedOrigins("*");
	}
}
