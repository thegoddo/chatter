package com.project.Chatter.repository;

import com.project.Chatter.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    /**
     * Finds the latest messages, ordered by timestamp descending.
     * We'll use this to load history when a user connects.
     * In a real-world scenario, you might use pagination here.
     * Spring Data JPA can infer the query from the method name.*
     *
     */
    List<ChatMessageEntity> findTop50ByOrderByTimestampDesc();

    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
            "(m.sender = ?1 AND m.recipient = ?2) OR " +
            "(m.sender = ?2 AND m.recipient = ?1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findPrivateMessages(String userA, String userB);
}
