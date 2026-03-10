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
import com.pdrosoft.matchmaking.chat.dto.NotificationDTO;
import com.pdrosoft.matchmaking.exception.NotFoundException;
import com.pdrosoft.matchmaking.stratego.enums.GamePhase;

@ExtendWith(MockitoExtension.class)
class GameNotificationWebSocketHandlerTest {

	private static final String PLAYERNAME1 = "player";
	private static final String ROOM_ID = "roomid";
	private static final String MESSAGE = "message";

	@Mock
	private ObjectMapper mapper;

	@InjectMocks
	private GameNotificationWebSocketHandler notificationHandler;

	@BeforeEach
	void initTest() {
		notificationHandler.getRooms().clear();
		notificationHandler.getUserSessions().clear();
	}

	private Map<String, Object> getTestAttributes() {
		return Map.of("username", PLAYERNAME1);
	}

	private String getTestUriQuery(String roomId) {
		return "q1=2&roomId=%s".formatted(roomId);
	}

	@Test
	void testHandleTextMessage() throws Exception {

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);
		var message = Mockito.mock(TextMessage.class);

		notificationHandler.handleTextMessage(session, message);

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		Mockito.verifyNoInteractions(session, message, mapper);
	}

	@Test
	void testAfterConnectionEstablished() throws IOException {
		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		// 1. Without session
		var session1 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session1.getAttributes()).thenReturn(attributes);
		Mockito.when(session1.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		notificationHandler.afterConnectionEstablished(session1);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsExactly(session1);
		assertThat(notificationHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session1);

		// 2. Renewing the session, old session already closed
		var session2 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session2.getAttributes()).thenReturn(attributes);
		Mockito.when(session2.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));
		Mockito.when(session1.isOpen()).thenReturn(false);
		notificationHandler.afterConnectionEstablished(session2);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session1, session2);
		assertThat(notificationHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session2);

		// 3. Renewing the session, old session open
		var session3 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session3.getAttributes()).thenReturn(attributes);
		Mockito.when(session3.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));
		Mockito.when(session2.isOpen()).thenReturn(true);
		notificationHandler.afterConnectionEstablished(session3);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session1, session2, session3);
		assertThat(notificationHandler.getUserSessions().get(PLAYERNAME1)).isEqualTo(session3);
		Mockito.verify(session2).close();

		Mockito.verifyNoMoreInteractions(session1, session2, session3, uri);
	}

	@Test
	void testAfterConnectionEstablishedEmptyRoom() throws Exception {

		notificationHandler.getRooms().put(ROOM_ID, Set.of());

		var session = Mockito.mock(WebSocketSession.class);

		var message = new TextMessage(MESSAGE);
		notificationHandler.handleTextMessage(session, message);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).isNullOrEmpty();

		Mockito.verifyNoMoreInteractions(session);
	}

	@Test
	void testAfterConnectionEstablishedWithOpenAndClosedConnections() throws Exception {

		var realMapper = new ObjectMapper();

		var notification = NotificationDTO.builder() //
				.gamePhase(GamePhase.PLAYING) //
				.message(MESSAGE) //
				.build();
		var textMessage = realMapper.writeValueAsString(notification);
		var sessionOpen = Mockito.mock(WebSocketSession.class);
		Mockito.when(sessionOpen.isOpen()).thenReturn(true);
		var sessionClosed = Mockito.mock(WebSocketSession.class);
		Mockito.when(sessionClosed.isOpen()).thenReturn(false);

		notificationHandler.getRooms().put(ROOM_ID, Set.of(sessionOpen, sessionClosed));

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(mapper.writeValueAsString(Mockito.any(NotificationDTO.class))).thenReturn(textMessage);

		notificationHandler.sendNotification(ROOM_ID, notification);

		var captor = ArgumentCaptor.forClass(TextMessage.class);
		Mockito.verify(sessionOpen).sendMessage(captor.capture());

		var messageDto = realMapper.readValue(captor.getValue().getPayload(), NotificationDTO.class);
		assertThat(messageDto.getGamePhase()).isEqualTo(GamePhase.PLAYING);
		assertThat(messageDto.getMessage()).isEqualTo(MESSAGE);

		Mockito.verifyNoMoreInteractions(session, sessionOpen, sessionClosed);
	}

	@Test
	void testHandleTransportErrorEmptyRooms() {
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.handleTransportError(session, exception);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).isNull();
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

		notificationHandler.getRooms().put(ROOM_ID, set);
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.handleTransportError(session, exception);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

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

		notificationHandler.getRooms().put(ROOM_ID, set);
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session, otherSession);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.handleTransportError(session, exception);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@Test
	void testAfterConnectionClosedEmptyRooms() {
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).isNull();
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

		notificationHandler.getRooms().put(ROOM_ID, set);
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

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

		notificationHandler.getRooms().put(ROOM_ID, set);
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsExactlyInAnyOrder(session, otherSession);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(getTestUriQuery(ROOM_ID));

		var exception = Mockito.mock(Exception.class);
		notificationHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA);

		assertThat(notificationHandler.getRooms().get(ROOM_ID)).doesNotContainAnyElementsOf(List.of(session));
		assertThat(notificationHandler.getRooms().get(ROOM_ID)).containsOnly(otherSession);

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testAfterConnectionEstablishedInvalidRoomId(String query) throws IOException {
		var attributes = getTestAttributes();
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getAttributes()).thenReturn(attributes);
		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);

		assertThatThrownBy(() -> notificationHandler.afterConnectionEstablished(session))
				.isInstanceOf(NotFoundException.class).hasMessage("Invalid room or room not found");

		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1", "q1=" })
	@NullSource
	void testHandleTransportErrorInvalidRoomId(String query) throws IOException {
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);
		var exception = Mockito.mock(Exception.class);

		assertThatThrownBy(() -> notificationHandler.handleTransportError(session, exception))
				.isInstanceOf(NotFoundException.class).hasMessage("Invalid room or room not found");

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

	@ParameterizedTest
	@ValueSource(strings = { "q1=2", "roomId1=1" })
	@NullSource
	void testAfterConnectionClosedInvalidRoomId(String query) throws IOException {
		var uri = Mockito.mock(URI.class);

		assertThat(notificationHandler.getUserSessions()).isEmpty();
		assertThat(notificationHandler.getRooms()).isEmpty();

		var session = Mockito.mock(WebSocketSession.class);

		Mockito.when(session.getUri()).thenReturn(uri);
		Mockito.when(uri.getQuery()).thenReturn(query);
		var exception = Mockito.mock(Exception.class);

		assertThatThrownBy(() -> notificationHandler.afterConnectionClosed(session, CloseStatus.BAD_DATA))
				.isInstanceOf(NotFoundException.class).hasMessage("Invalid room or room not found");

		Mockito.verifyNoInteractions(exception);
		Mockito.verifyNoMoreInteractions(session, uri);
	}

}
