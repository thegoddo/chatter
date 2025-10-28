package com.project.Chatter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * FIX: This method defines the PasswordEncoder bean, resolving the 'No qualifying bean' error.
     * We use BCryptPasswordEncoder, which is the standard strong hashing algorithm.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the AuthenticationManager bean, which is required by AuthService
     * to perform the user login process.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain (the main security rules).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF protection for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // 2. Set session management to stateless (essential for JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to all authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // All other requests require authentication (a valid JWT)
                        .anyRequest().authenticated()
                );

        // NOTE: In Phase 2, we will add the JWT filter here before the filter chain is built.

        return http.build();
    }
}
