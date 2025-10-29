package com.project.Chatter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
