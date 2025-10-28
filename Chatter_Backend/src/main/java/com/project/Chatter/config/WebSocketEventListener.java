package com.project.Chatter.config;

import com.project.Chatter.services.PresenseService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    // Used to send messages to broker outside of WebSocket controllers
    private final SimpMessageSendingOperations messageTemplate;
    private PresenseService presenseService;
    public WebSocketEventListener(SimpMessageSendingOperations messageTemplate, PresenseService presenseService) {
        this.messageTemplate = messageTemplate;
        this.presenseService = presenseService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(org.springframework.web.socket.messaging.SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            System.out.println("User Disconnected : " + username);

            com.project.Chatter.dto.ChatMessage chatMessage = new com.project.Chatter.dto.ChatMessage();
            chatMessage.setType(com.project.Chatter.dto.ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setContent(username + " left the chat");
            chatMessage.setTimestamp(LocalDateTime.now());

            // Notify all subscribers that the user has left
            messageTemplate.convertAndSend("/topic/public-chat", chatMessage);
        }
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if(username != null && headerAccessor.getDestination() != null) {
            if(headerAccessor.getDestination().equals("/topic/public-chat")) {
                presenseService.userConnected(username);
                messageTemplate.convertAndSend("/topic/online-users", presenseService.getOnlineUsers());
            }
        }
    }
}
