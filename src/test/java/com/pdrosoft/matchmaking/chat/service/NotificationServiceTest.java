package com.pdrosoft.matchmaking.chat.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pdrosoft.matchmaking.chat.config.GameNotificationWebSocketHandler;
import com.pdrosoft.matchmaking.chat.dto.NotificationDTO;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	private static final String ROOM_ID = "roomid";

	@Mock
	private GameNotificationWebSocketHandler gameNotificationWebSocketHandler;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	void testSendNotificationSuccess() throws Exception {

		var notification = Mockito.mock(NotificationDTO.class);

		notificationService.sendNotification(ROOM_ID, notification);

		Mockito.verify(gameNotificationWebSocketHandler).sendNotification(ROOM_ID, notification);

		Mockito.verifyNoInteractions(notification);
		Mockito.verifyNoMoreInteractions(gameNotificationWebSocketHandler);
	}
	
	@Test
	void testSendNotificationException() throws Exception {

		var notification = Mockito.mock(NotificationDTO.class);

		Mockito.doThrow(new IOException("ioexception")).when(gameNotificationWebSocketHandler).sendNotification(ROOM_ID, notification);

		notificationService.sendNotification(ROOM_ID, notification);

		Mockito.verifyNoInteractions(notification);
		Mockito.verifyNoMoreInteractions(gameNotificationWebSocketHandler);
	}

}
