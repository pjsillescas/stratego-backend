package com.pdrosoft.matchmaking.chat.config;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class GameWebSocketHandler extends TextWebSocketHandler {

	@NonNull
	private final ObjectMapper mapper = new ObjectMapper();

	private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		String username = (String) session.getAttributes().get("username");
		String roomId = getRoomId(session);

		// Close previous session if exists
		if (userSessions.containsKey(username)) {
			WebSocketSession oldSession = userSessions.get(username);
			if (oldSession != null && oldSession.isOpen()) {
				oldSession.close();
			}
		}

		userSessions.put(username, session);

		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

		System.out.println(username + " connected to room " + roomId);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		String roomId = getRoomId(session);
		String username = (String) session.getAttributes().get("username");

		String formattedMessage = username + ": " + message.getPayload();

		for (WebSocketSession s : rooms.get(roomId)) {
			if (s.isOpen()) {
				s.sendMessage(new TextMessage(formattedMessage));
			}
		}
	}

	private void cleanup(WebSocketSession session) {
		// sessions.remove(session);
		String roomId = getRoomId(session);
		rooms.getOrDefault(roomId, Set.of()).remove(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		cleanup(session);
		System.out.println("Player disconnected");
	}

	private String getRoomId(WebSocketSession session) {
		return session.getUri().getQuery().split("=")[1];
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		cleanup(session);
	}
}