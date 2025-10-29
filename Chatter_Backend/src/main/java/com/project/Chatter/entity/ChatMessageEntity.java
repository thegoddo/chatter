package com.project.Chatter.entity;

import com.project.Chatter.dto.ChatMessage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type;

    @Column(columnDefinition = "TEXT")
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime timestamp;
}
