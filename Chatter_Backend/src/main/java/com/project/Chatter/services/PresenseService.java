package com.project.Chatter.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PresenseService {
    private static final String ONLINE_USERS_ONLY = "chat:online_users";

    private final RedisTemplate<String, Object> redisTemplate;

    public PresenseService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void userConnected(String username) {
        redisTemplate.opsForSet().add(ONLINE_USERS_ONLY, username);
        System.out.println("User connected and added to Redis: " + username);
    }

    public void userDisconnected(String username) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_ONLY, username);
        System.out.println("User disconnected and removed from Redis: " + username);
    }

    public Set<String> getOnlineUsers() {
        Set<Object> members = redisTemplate.opsForSet().members(ONLINE_USERS_ONLY);

        if (members == null) {
            return Set.of();
        }

        return members.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toSet());
    }
}
