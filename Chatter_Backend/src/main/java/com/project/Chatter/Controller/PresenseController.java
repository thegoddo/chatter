package com.project.Chatter.Controller;

import com.project.Chatter.services.PresenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenseController {
    private final PresenseService presenseService;

    public PresenseController(PresenseService presenseService) {
        this.presenseService = presenseService;
    }

    @GetMapping("/online")
    public Set<String> getAllUsers() {
        return presenseService.getOnlineUsers();
    }

}
