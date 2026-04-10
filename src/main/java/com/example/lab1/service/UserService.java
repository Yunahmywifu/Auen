package com.example.lab1.service;

import com.example.lab1.model.User;
import com.example.lab1.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password, String email) {
        String normalizedUsername = username == null ? null : username.trim();
        String normalizedEmail = email == null ? null : email.trim();

        if (normalizedUsername == null || normalizedUsername.length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        if (normalizedEmail != null && !normalizedEmail.isBlank()) {
            user.setEmail(normalizedEmail);
        }

        return userRepository.save(user);
    }
}

