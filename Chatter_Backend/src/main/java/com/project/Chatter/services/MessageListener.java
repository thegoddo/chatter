package com.project.Chatter.services;

import com.project.Chatter.config.KafkaConfig;
import com.project.Chatter.dto.ChatMessage;
import com.project.Chatter.repository.ChatMessageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {

    private final ChatMessageRepository chatMessageRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageListener(ChatMessageRepository chatMessageRepository, MessageMapper messageMapper, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = KafkaConfig.PUBLIC_CHAT_TOPIC, groupId = "chatter-group-public")
    public void listenPublic(ChatMessage chatMessage) {
        chatMessageRepository.save(messageMapper.toEntity(chatMessage));
        messagingTemplate.convertAndSend("/topic/public-chat", chatMessage);
    }

    @KafkaListener(topics = KafkaConfig.PRIVATE_CHAT_TOPIC, groupId = "chatter-group-private")
    public void listenPrivate(ChatMessage chatMessage) {
        chatMessageRepository.save(messageMapper.toEntity(chatMessage));
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(),
                "/queue/messages",
                chatMessage
        );

        // Optionally, send to sender's queue as well
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(),
                "/queue/messages",
                chatMessage
        );
    }
}
