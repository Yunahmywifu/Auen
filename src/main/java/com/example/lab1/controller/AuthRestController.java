package com.example.lab1.controller;

import com.example.lab1.model.User;
import com.example.lab1.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {
        User user = userService.register(body.getUsername(), body.getPassword(), body.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Пользователь успешно создан",
                        "username", user.getUsername(),
                        "role", user.getRole()
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Не аутентифицирован"));
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER");

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "role", role
        ));
    }

    public static class RegisterRequest {
        @NotNull
        @Size(min = 3, max = 50)
        private String username;

        @NotNull
        @Size(min = 6)
        private String password;

        @Email
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
