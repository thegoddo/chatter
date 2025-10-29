package com.project.Chatter.Controller;

import com.project.Chatter.services.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {
    private final PresenceService presenseService;

    public PresenceController(PresenceService presenceService) {
        this.presenseService = presenceService;
    }

    @GetMapping("/online")
    public ResponseEntity<Set<String>> getAllUsers() {
        Set<String> onlineUsers = presenseService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

}
