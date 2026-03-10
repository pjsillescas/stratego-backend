package com.pdrosoft.matchmaking.chat.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pdrosoft.matchmaking.chat.config.GameNotificationWebSocketHandler;
import com.pdrosoft.matchmaking.chat.dto.NotificationDTO;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class NotificationServiceImpl implements NotificationService {

	@NonNull
	private final GameNotificationWebSocketHandler notificationWebsocketHandler;

	@Override
	public void sendNotification(String roomId, NotificationDTO notification) {
		try {
			notificationWebsocketHandler.sendNotification(roomId, notification);
		} catch (IOException e) {
			log.debug("Error sending notification", e);
		}
		
	}

}
