package com.project.Chatter.config;

import com.project.Chatter.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Configures the Spring WebSocket handler and enables the STOMP message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtils jwtUtils;

    public WebSocketConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers the STOMP endpoint (/ws) that the clients use to connect to the WebSocket server.
     * We explicitly allow the Next.js development server origin (localhost:3000) for CORS.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Expose the /ws endpoint for WebSocket connections
        registry.addEndpoint("/chat")
                .setAllowedOrigins("http://localhost:3000") // Allow connection from frontend
                .withSockJS(); // Enable SockJS fallback for wider browser compatibility
    }

    /**
     * Configures the message broker, which handles routing messages to clients.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Destination prefixes for messages sent *from* the server *to* the client (subscriptions)
        // /topic for public messages, /queue for user-specific messages
        registry.enableSimpleBroker("/topic", "/queue");

        // Destination prefixes for messages sent *from* the client *to* the server (application logic)
        registry.setApplicationDestinationPrefixes("/app");

        // Designates the user destination prefix for private messaging
        registry.setUserDestinationPrefix("/user");
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtils.extractEmail(token);
                        accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));
                        System.out.println("✅ WebSocket authenticated as: " + username);
                    }
                }
                return message;
            }
        });
    }
}
