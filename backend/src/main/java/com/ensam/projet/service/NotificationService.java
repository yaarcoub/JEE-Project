package com.ensam.projet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String title, String message, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("message", message);
        payload.put("type", type); // e.g., "INFO", "SUCCESS", "WARNING"

        messagingTemplate.convertAndSend("/topic/notifications", payload);
    }
}
