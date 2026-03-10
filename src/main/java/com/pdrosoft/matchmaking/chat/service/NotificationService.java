package com.pdrosoft.matchmaking.chat.service;

import com.pdrosoft.matchmaking.chat.dto.NotificationDTO;

public interface NotificationService {
	void sendNotification(String roomId, NotificationDTO notification);
}
