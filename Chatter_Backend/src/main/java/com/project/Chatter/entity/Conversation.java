package com.project.Chatter.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;

@Entity
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private boolean isGroupChat;

    @ManyToMany
    @JoinTable(
            name = "conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;
}
