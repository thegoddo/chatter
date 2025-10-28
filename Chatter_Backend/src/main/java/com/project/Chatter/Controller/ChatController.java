package com.project.Chatter.Controller;

import com.project.Chatter.config.KafkaConfig;
import com.project.Chatter.dto.ChatMessage;
import com.project.Chatter.entity.ChatMessageEntity;
import com.project.Chatter.repository.ChatMessageRepository;
import com.project.Chatter.services.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RestController
@RequestMapping("/api/messages")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final MessageMapper messageMapper;
    private SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

//    @Autowired
    public ChatController(ChatMessageRepository chatMessageRepository, MessageMapper messageMapper, SimpMessagingTemplate messagingTemplate, KafkaTemplate kafkaTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        kafkaTemplate.send(KafkaConfig.PUBLIC_CHAT_TOPIC, chatMessage.getSender(), chatMessage);
    }
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatMessage.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(KafkaConfig.PRIVATE_CHAT_TOPIC, chatMessage.getSender(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public-chat")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setContent(chatMessage.getSender() + " joined the chat");
        System.out.println("User joined: " + chatMessage.getSender());

        return chatMessage;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        List<ChatMessageEntity> historyEntities = chatMessageRepository.findTop50ByOrderByTimestampDesc();
        List<ChatMessage> historyDtos = historyEntities.stream()
                .map(messageMapper::toDto)
                .toList();

        Collections.reverse(historyDtos); // Optional: Reverse to show oldest first
        return ResponseEntity.ok(historyDtos);
    }

    @GetMapping("/history/private/{userA}/{userB}")
    public ResponseEntity<List<ChatMessage>> getPrivateChatHistory(@PathVariable String userA,@PathVariable String userB) {
        List<ChatMessageEntity> historyEntities = chatMessageRepository.findPrivateMessages(userA, userB);
        List<ChatMessage> historyDtos = historyEntities.stream()
                .map(messageMapper::toDto)
                .toList();
        return ResponseEntity.ok(historyDtos);
    }
}
