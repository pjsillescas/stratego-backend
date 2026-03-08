package com.pdrosoft.matchmaking.chat.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import com.pdrosoft.matchmaking.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class JwtHandshakeInterceptorTest {

	private static final String INVALID_TOKEN = "invalid-token";
	private static final String VALID_TOKEN = "valid-token";
	private static final String PLAYER_NAME = "player";

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private JwtHandshakeInterceptor handshakeInterceptor;

	@Test
	void testBeforeHandshakeNoServletRequest() throws Exception {
		var request = Mockito.mock(ServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		var attributes = new HashMap<String, Object>();

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isFalse();
		assertThat(attributes.isEmpty()).isTrue();

		Mockito.verifyNoInteractions(request, response, wsHandler, jwtUtil);
	}

	@ParameterizedTest
	@ValueSource(strings = { "invalid-value", "" })
	@NullSource
	void testBeforeHandshakeWithInvalidHeader(String headerValue) throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(headerValue);
		Mockito.when(servletRequest.getQueryString()).thenReturn(null);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isFalse();
		assertThat(attributes.isEmpty()).isTrue();

		Mockito.verifyNoInteractions(response, wsHandler, jwtUtil);
		Mockito.verifyNoMoreInteractions(request, servletRequest);
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "q1=1", "tokenn=invalid-value", "token=" })
	@NullSource
	void testBeforeHandshakeWithInvalidQueryString(String queryString) throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(null);
		Mockito.when(servletRequest.getQueryString()).thenReturn(queryString);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isFalse();
		assertThat(attributes.isEmpty()).isTrue();

		Mockito.verifyNoInteractions(response, wsHandler, jwtUtil);
		Mockito.verifyNoMoreInteractions(request, servletRequest);
	}

	private String getBearer(String token) {
		return "Bearer %s".formatted(token);
	}

	@Test
	void testBeforeHandshakeInvalidTokenFromHeader() throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(getBearer(INVALID_TOKEN));
		Mockito.when(jwtUtil.isTokenValid(INVALID_TOKEN)).thenReturn(false);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isFalse();
		assertThat(attributes.isEmpty()).isTrue();

		Mockito.verifyNoInteractions(response, wsHandler);
		Mockito.verifyNoMoreInteractions(request, servletRequest, jwtUtil);
	}

	@Test
	void testBeforeHandshakeValidTokenFromHeader() throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(getBearer(VALID_TOKEN));
		Mockito.when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
		Mockito.when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(PLAYER_NAME);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isTrue();
		assertThat(attributes.isEmpty()).isFalse();
		assertThat(attributes.get("username")).isEqualTo(PLAYER_NAME);

		Mockito.verifyNoInteractions(response, wsHandler);
		Mockito.verifyNoMoreInteractions(request, servletRequest, jwtUtil);
	}

	@Test
	void testBeforeHandshakeInvalidTokenFromQueryString() throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(null);
		Mockito.when(servletRequest.getQueryString()).thenReturn("token=%s".formatted(INVALID_TOKEN));
		Mockito.when(jwtUtil.isTokenValid(INVALID_TOKEN)).thenReturn(false);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isFalse();
		assertThat(attributes.isEmpty()).isTrue();

		Mockito.verifyNoInteractions(response, wsHandler);
		Mockito.verifyNoMoreInteractions(request, servletRequest, jwtUtil);
	}

	@Test
	void testBeforeHandshakeValidTokenFromQueryString() throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<String, Object>();

		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletRequest()).thenReturn(servletRequest);

		Mockito.when(servletRequest.getHeader("Authorization")).thenReturn(null);
		Mockito.when(servletRequest.getQueryString()).thenReturn("token=%s".formatted(VALID_TOKEN));
		Mockito.when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
		Mockito.when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(PLAYER_NAME);

		assertThat(handshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)).isTrue();
		assertThat(attributes.isEmpty()).isFalse();
		assertThat(attributes.get("username")).isEqualTo(PLAYER_NAME);

		Mockito.verifyNoInteractions(response, wsHandler);
		Mockito.verifyNoMoreInteractions(request, servletRequest, jwtUtil);
	}

	@Test
	void testAfterHandshake() throws Exception {
		var request = Mockito.mock(ServletServerHttpRequest.class);
		var response = Mockito.mock(ServerHttpResponse.class);
		var wsHandler = Mockito.mock(WebSocketHandler.class);

		handshakeInterceptor.afterHandshake(request, response, wsHandler, null);

		Mockito.verifyNoInteractions(response, wsHandler, request, jwtUtil);
	}

}
