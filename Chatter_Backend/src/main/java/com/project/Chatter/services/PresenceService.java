package com.project.Chatter.services;

import com.project.Chatter.entity.Status;
import com.project.Chatter.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PresenceService {
    private static final String ONLINE_USERS_ONLY = "chat:online_users";

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public PresenceService(RedisTemplate<String, Object> redisTemplate,
                           UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @Transactional
    public void userConnected(String username) {
        userRepository.updateStatusByUsername(username, Status.ONLINE);
        redisTemplate.opsForSet().add(ONLINE_USERS_ONLY, username);
        System.out.println("User connected and added to Redis: " + username);
    }

    @Transactional
    public void userDisconnected(String username) {
        userRepository.updateStatusByUsername(username, Status.OFFLINE);
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
