package com.project.Chatter.services;
import com.project.Chatter.entity.User;
import com.project.Chatter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Implements the core UserDetailsService method.
     * This is called by the AuthenticationManager to retrieve a user's details (username, password, roles)
     * during the login process.
     * * NOTE: This assumes your UserRepository has a findByUsername method returning Optional<User>.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Find the application's user entity
        User applicationUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Map the application's User entity to Spring Security's UserDetails object.
        // We grant a default 'ROLE_USER' authority for basic authentication purposes.
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new org.springframework.security.core.userdetails.User(
                applicationUser.getUsername(),
                applicationUser.getPassword(), // The stored (hashed) password
                authorities // The user's roles/permissions
        );
    }
}
