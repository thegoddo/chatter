package com.project.Chatter.config;

import com.project.Chatter.utils.JwtUtils;
import com.project.Chatter.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter responsible for validating JWTs and setting the Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtils jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Check for Authorization header and JWT format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT
        jwt = authHeader.substring(7);

        // 3. Extract username (which is email in JwtUtils) from JWT
        try {
            username = jwtUtil.extractEmail(jwt);
            System.out.println("DEBUG FILTER: Successfully extracted username from JWT: " + username);
        } catch (Exception e) {
            // This catches initial token parsing issues
            System.err.println("JWT Extraction FAILED (Token is invalid or expired at extractEmail call). Message: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Validate user and set Security Context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // This is the step you confirmed works.
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            // DEBUG: Print token and username just before validation call
            System.out.println("DEBUG FILTER: Attempting final validation check.");
            System.out.println("DEBUG FILTER: Username: " + username);
            System.out.println("DEBUG FILTER: JWT (Partial): " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

            // Token is validated again, and if valid, the SecurityContext is set.
            if (jwtUtil.validateToken(jwt, username)) {

                // Token is valid; create authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                // Attach request details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Set the security context, granting the request access
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("DEBUG FILTER: Security Context set successfully.");
            } else {
                System.out.println("DEBUG FILTER: Token INVALIDATED by jwtUtil.validateToken().");
            }
        }

        // 5. Continue filter chain execution
        filterChain.doFilter(request, response);
    }
}
