package com.project.Chatter.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class ChatMessage {

    public enum MessageType {
        CHAT, // A standard user message
        JOIN, // Notification of a user joining the chat
        LEAVE // Notification of a user leaving the chat
    }

    private MessageType type;
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime timestamp;
}
