package com.project.Chatter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    private String content;
    private Long timestamp;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
}
