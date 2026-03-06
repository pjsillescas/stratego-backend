package com.pdrosoft.matchmaking.chat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdrosoft.matchmaking.chat.dto.ChatMessageDTO;
import com.pdrosoft.matchmaking.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
public class GameWebSocketHandlerTest {

	private static final String PLAYERNAME1 = "player";
	private static final String ROOM_ID = "roomid";

	@Mock
	private ObjectMapper mapper;

	@InjectMocks
	private GameWebSocketHandler gameHandler;

	@BeforeEach
	void initTest() {
		gameHandler.getRooms().clear();
		gameHandler.getUserSessions().clear();
	}

	private Map<String, Object> getTestAttributes() {
		return Map.of("username", PLAYERNAME1);
	}

	private String getTestUriQuery(String roomId) {
		return "q1=2&roomId=%s".formatted(roomId);
	}

	@Test
	void testAfterConnectionEstablished() throws IOException {
		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getUserSessions()).isEmpty();
		assertThat(gameHandler.getRooms()).isEmpty();

		// 1. Without session
		var session1 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session1.getAttributes()).thenReturn(attributes);
		Mockito.when(session1.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		gameHandler.afterConnectionEstablished(session1);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsExactly(session1);
		assertThat(gameHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session1);

		// 2. Renewing the session, old session already closed
		var session2 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session2.getAttributes()).thenReturn(attributes);
		Mockito.when(session2.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));
		Mockito.when(session1.isOpen()).thenReturn(false);
		gameHandler.afterConnectionEstablished(session2);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session1, session2);
		assertThat(gameHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session2);

		// 3. Renewing the session, old session open
		var session3 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session3.getAttributes()).thenReturn(attributes);
		Mockito.when(session3.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));
		Mockito.when(session2.isOpen()).thenReturn(true);
		gameHandler.afterConnectionEstablished(session3);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session1, session2, session3);
		assertThat(gameHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session3);
		Mockito.verify(session2).close();

		Mockito.verifyNoMoreInteractions(session1, session2, session3, uri);
	}

	private static final String MESSAGE = "message";

	@Test
	void testAfterConnectionEstablishedEmptyRoom() throws Exception {

		gameHandler.getRooms().put(ROOM_ID, Set.of());

		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getAttributes()).thenReturn(attributes);
		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var message = new TextMessage(MESSAGE);
		gameHandler.handleTextMessage(session, message);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).isNullOrEmpty();

		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testAfterConnectionEstablishedWithOpenAndClosedConnections() throws Exception {

		var realMapper = new ObjectMapper();
		
		var messageDtoInitial = ChatMessageDTO.builder() //
				.player(PLAYERNAME1) //
				.message(MESSAGE) //
				.build();
		var textMessage = realMapper.writeValueAsString(messageDtoInitial);
		var sessionOpen = Mockito.mock(WebSocketSession.class);
		Mockito.when(sessionOpen.isOpen()).thenReturn(true);
		var sessionClosed = Mockito.mock(WebSocketSession.class);
		Mockito.when(sessionClosed.isOpen()).thenReturn(false);

		gameHandler.getRooms().put(ROOM_ID, Set.of(sessionOpen, sessionClosed));

		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getAttributes()).thenReturn(attributes);
		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));
		Mockito.when(mapper.writeValueAsString(Mockito.any(ChatMessageDTO.class))).thenReturn(textMessage);

		var message = new TextMessage(MESSAGE);
		gameHandler.handleTextMessage(session, message);

		var captor = ArgumentCaptor.forClass(TextMessage.class);
		Mockito.verify(sessionOpen).sendMessage(captor.capture());

		var messageDto = realMapper.readValue(captor.getValue().getPayload(), ChatMessageDTO.class);
		//assertThat(captor.getValue().getPayload()).isEqualTo("{\"player\":\"%s\",\"message\"}".formatted(PLAYERNAME1, MESSAGE));
		assertThat(messageDto.getPlayer()).isEqualTo(PLAYERNAME1);
		assertThat(messageDto.getMessage()).isEqualTo(MESSAGE);

		Mockito.verifyNoMoreInteractions(session, sessionOpen, sessionClosed, uri);
	}

	@Test
	void testHandleTransportErrorEmptyRooms() {
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.handleTransportError(session, exception);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).isNull();
		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testHandleTransportErrorOtherSession() {
		var uri = Mockito.mock(URI.class);

		var otherSession = Mockito.mock(WebSocketSession.class);
		var session = Mockito.mock(WebSocketSession.class);

		var set = new HashSet<WebSocketSession>();
		set.add(otherSession);

		gameHandler.getRooms().put(ROOM_ID, set);
		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.handleTransportError(session, exception);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testHandleTransportErrorSameSession() {
		var uri = Mockito.mock(URI.class);

		var otherSession = Mockito.mock(WebSocketSession.class);
		var session = Mockito.mock(WebSocketSession.class);

		var set = new HashSet<WebSocketSession>();
		set.add(otherSession);
		set.add(session);

		gameHandler.getRooms().put(ROOM_ID, set);
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session, otherSession);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.handleTransportError(session, exception);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testAfterConnectionClosedEmptyRooms() {
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).isNull();
		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testAfterConnectionClosedOtherSession() {
		var uri = Mockito.mock(URI.class);

		var otherSession = Mockito.mock(WebSocketSession.class);
		var session = Mockito.mock(WebSocketSession.class);

		var set = new HashSet<WebSocketSession>();
		set.add(otherSession);

		gameHandler.getRooms().put(ROOM_ID, set);
		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testAfterConnectionClosedSameSession() {
		var uri = Mockito.mock(URI.class);

		var otherSession = Mockito.mock(WebSocketSession.class);
		var session = Mockito.mock(WebSocketSession.class);

		var set = new HashSet<WebSocketSession>();
		set.add(otherSession);
		set.add(session);

		gameHandler.getRooms().put(ROOM_ID, set);
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session, otherSession);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		gameHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(gameHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(gameHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testAfterConnectionEstablishedInvalidRoomId(String query) throws IOException {
		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getUserSessions()).isEmpty();
		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getAttributes()).thenReturn(attributes);
		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);

		assertThatThrownBy(() -> gameHandler.afterConnectionEstablished(session)).isInstanceOf(NotFoundException.class)
				.hasMessage("Invalid room or room not found");

		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testHandleTextMessageInvalidRoomId(String query) throws IOException {
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getUserSessions()).isEmpty();
		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);
		var message = Mockito.mock(TextMessage.class);

		assertThatThrownBy(() -> gameHandler.handleTextMessage(session, message)).isInstanceOf(NotFoundException.class)
				.hasMessage("Invalid room or room not found");

		Mockito.verifyNoInteractions(message);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testHandleTransportErrorInvalidRoomId(String query) throws IOException {
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getUserSessions()).isEmpty();
		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);
		var exception = Mockito.mock(Exception.class);

		assertThatThrownBy(() -> gameHandler.handleTransportError(session, exception))
				.isInstanceOf(NotFoundException.class).hasMessage("Invalid room or room not found");

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testAfterConnectionClosedInvalidRoomId(String query) throws IOException {
		var uri = Mockito.mock(URI.class);

		assertThat(gameHandler.getUserSessions()).isEmpty();
		assertThat(gameHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);
		var exception = Mockito.mock(Exception.class);

		assertThatThrownBy(() -> gameHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA))
				.isInstanceOf(NotFoundException.class).hasMessage("Invalid room or room not found");

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

}
