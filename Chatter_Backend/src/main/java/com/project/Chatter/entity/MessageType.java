package com.project.Chatter.entity;

public enum MessageType {
    /**
     * Enum defining the purpose of the message.
     */
        CHAT,     // Regular public message
        JOIN,     // User connecting to the chat
        LEAVE,    // User disconnecting from the chat
        IMAGE,
        PRIVATE   // Private message between two users (The Fix!)
}
