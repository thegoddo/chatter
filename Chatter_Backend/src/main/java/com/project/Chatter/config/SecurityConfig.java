package com.project.Chatter.config;

import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from the Next.js development server
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        // Allow common headers, including Authorization for JWT
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // Allow all necessary HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true); // Crucial for sending cookies/auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to ALL paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        // 1. Disable CSRF protection for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // 2. Set session management to stateless (essential for JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to all authentication endpoints
                        .requestMatchers("/**").permitAll()
//                        .requestMatchers("/api/auth/**", "ws/**", "/api/presence/**", "/api/messages/**").permitAll()
//                        // All other requests require authentication (a valid JWT)
//                        .requestMatchers("/api/presence/**", "/api/chat/**", "/api/messages/**").authenticated()
//                        .anyRequest().authenticated()
                );

        // NOTE: In Phase 2, we will add the JWT filter here before the filter chain is built.

        return http.build();
    }
}
