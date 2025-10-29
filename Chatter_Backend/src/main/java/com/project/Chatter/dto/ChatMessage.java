package com.project.Chatter.dto;

import com.project.Chatter.entity.MessageType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for chat messages, used for WebSocket and REST communication.
 */
@Data
public class ChatMessage {

    private MessageType type;
    private String content;
    private String sender;
    private String recipient; // Added for private messages
    private String mediaUrl;
    private LocalDateTime timestamp;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
}