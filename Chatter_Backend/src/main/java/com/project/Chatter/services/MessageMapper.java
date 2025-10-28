package com.project.Chatter.services;

import com.project.Chatter.dto.ChatMessage;
import com.project.Chatter.entity.ChatMessageEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageMapper {

    public ChatMessageEntity toEntity(ChatMessage dto) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSender(dto.getSender());
        entity.setContent(dto.getContent());
        entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        entity.setType(dto.getType());
        entity.setRecipient(dto.getRecipient());
        return entity;
    }

    public ChatMessage toDto(ChatMessageEntity entity) {
        ChatMessage dto = new ChatMessage();
        dto.setSender(entity.getSender());
        dto.setContent(entity.getContent());
        dto.setTimestamp(entity.getTimestamp());
        dto.setType(entity.getType());
        dto.setRecipient(entity.getRecipient());
        return dto;
    }
}
