package com.pdrosoft.matchmaking.chat.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdrosoft.matchmaking.chat.dto.NotificationDTO;
import com.pdrosoft.matchmaking.exception.NotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
@Slf4j
public class GameNotificationWebSocketHandler extends TextWebSocketHandler {

	@NonNull
	private final ObjectMapper mapper;

	private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

	public Map<String, WebSocketSession> getUserSessions() {
		return userSessions;
	}

	public Map<String, Set<WebSocketSession>> getRooms() {
		return rooms;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		String username = (String) session.getAttributes().get("username");
		String roomId = getRoomId(session);

		// Close previous session if exists
		if (userSessions.containsKey(username)) {
			var optSession = Optional.ofNullable(userSessions.get(username)).filter(oldSession -> oldSession.isOpen());
			if (optSession.isPresent()) {
				optSession.get().close();
			}
		}

		userSessions.put(username, session);

		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

		log.debug(username + " connected to room " + roomId);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		;
	}
	
	public void sendNotification(String roomId, NotificationDTO notification) throws IOException {
		var formattedMessage = mapper.writeValueAsString(notification);
		for (WebSocketSession s : CollectionUtils.emptyIfNull(rooms.get(roomId))) {
			if (s.isOpen()) {
				s.sendMessage(new TextMessage(formattedMessage));
			}
		}
	}

	private void cleanup(WebSocketSession session) {
		String roomId = getRoomId(session);
		rooms.getOrDefault(roomId, new HashSet<WebSocketSession>()).remove(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		cleanup(session);
		log.debug("Player disconnected");
	}

	private Map<String, String> parseQueryString(String query) {
		var map = new HashMap<String, String>();
		Optional.ofNullable(query).map(str -> List.of(str.split("&"))).orElse(List.of()) //
				.forEach(param -> Optional.ofNullable(param).map(par -> par.split("="))
						.ifPresent(pair -> map.put(pair[0], pair.length == 2 ? pair[1] : null)));
		return map;
	}

	private String getRoomId(WebSocketSession session) {
		return Optional.ofNullable(session.getUri().getQuery()).map(this::parseQueryString)
				.map(map -> map.get("roomId")) //
				.orElseThrow(() -> new NotFoundException("Invalid room or room not found"));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		cleanup(session);
	}
}