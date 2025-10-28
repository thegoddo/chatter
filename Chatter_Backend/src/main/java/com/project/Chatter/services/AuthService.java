package com.project.Chatter.services;

import com.project.Chatter.dto.LoginRequest;
import com.project.Chatter.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtil;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public String authenticateAndGenerateToken(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        if(authentication.isAuthenticated()) {
            return jwtUtil.generateToken(loginRequest.getUsername());
        } else {
            throw new RuntimeException("Invalid Login Credentials");
        }
    }
}
