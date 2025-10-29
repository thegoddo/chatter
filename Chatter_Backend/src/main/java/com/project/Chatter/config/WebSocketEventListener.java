package com.project.Chatter.config;

import com.project.Chatter.services.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    // You would typically have a SessionConnectEvent listener here, but for now, we focus on the disconnect fix.
    // ...
    @EventListener
    public void handleWebSocketConnectListener(SessionDisconnectEvent event) {
        // This method can be implemented to handle connection events if needed.
        // Wrap the event message in a STOMP accessor
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // --- FIX FOR LINE 27 ---
        // 1. Get session attributes. This method can return null if the session was never established.
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        // Safety Check: If sessionAttributes is null, we cannot proceed.
        if (sessionAttributes == null) {
            System.err.println("WebSocket connected event detected without session attributes. Skipping user status update.");
            return;
        }

        // 2. Safely extract the username
        String username = (String) sessionAttributes.get("username");

        // 3. Process disconnection if username exists
        if (username != null) {
            System.out.println("User Connected: " + username);
            // Call the service to update the persistent status and broadcast
            presenceService.userConnected(username);
        }
    }

    /**
     * Handles the disconnection event, updating the user's status to OFFLINE.
     * This method was throwing a NullPointerException on line 27.
     * @param event The session disconnect event.
     */
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        // Wrap the event message in a STOMP accessor
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//
//        // --- FIX FOR LINE 27 ---
//        // 1. Get session attributes. This method can return null if the session was never established.
//        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
//
//        // Safety Check: If sessionAttributes is null, we cannot proceed.
//        if (sessionAttributes == null) {
//            System.err.println("WebSocket disconnect event detected without session attributes. Skipping user status update.");
//            return;
//        }
//
//        // 2. Safely extract the username
//        String username = (String) sessionAttributes.get("username");
//
//        // 3. Process disconnection if username exists
//        if (username != null) {
//            System.out.println("User disconnected: " + username);
//            // Call the service to update the persistent status and broadcast
//            presenceService.userDisconnected(username);
//        }
//    }
}