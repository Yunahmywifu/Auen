package com.example.lab1.model;

import java.time.LocalDateTime;

public class PendingRegistration {

    private String email;
    private String username;
    private String encodedPassword;
    private String confirmationCode;
    private LocalDateTime expiresAt;

    public PendingRegistration() {}

    public PendingRegistration(String email, String username,
                                String encodedPassword, String confirmationCode,
                                LocalDateTime expiresAt) {
        this.email = email;
        this.username = username;
        this.encodedPassword = encodedPassword;
        this.confirmationCode = confirmationCode;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncodedPassword() { return encodedPassword; }
    public void setEncodedPassword(String encodedPassword) { this.encodedPassword = encodedPassword; }

    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}

