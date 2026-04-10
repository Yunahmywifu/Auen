package com.example.lab1.controller;

import com.example.lab1.model.Song;
import com.example.lab1.model.User;
import com.example.lab1.repository.ArtistRepository;
import com.example.lab1.repository.SongRepository;
import com.example.lab1.repository.UserRepository;
import com.example.lab1.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public UserProfileController(UserRepository userRepository,
                                 ArtistRepository artistRepository,
                                 SongRepository songRepository,
                                 PasswordEncoder passwordEncoder,
                                 CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping(value = "/data", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getProfileData(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "not authenticated"));
        }

        User user = userRepository
                .findByUsername(userDetails.getUsername())
                .orElseThrow();

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");
        data.put("email", user.getEmail() != null ? user.getEmail() : "");
        data.put("favoriteGenre", user.getFavoriteGenre() != null ? user.getFavoriteGenre() : "");
        data.put("favoriteArtist", user.getFavoriteArtist() != null ? user.getFavoriteArtist() : "");
        data.put("avatarBase64", user.getAvatarBase64() != null ? user.getAvatarBase64() : "");

        return ResponseEntity.ok(data);
    }

    @GetMapping(value = "/artists", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getArtists() {
        List<Map<String, Object>> artists = new ArrayList<>();
        for (var a : artistRepository.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", a.getId());
            row.put("name", a.getName());
            artists.add(row);
        }
        return ResponseEntity.ok(artists);
    }

    @GetMapping(value = "/recent-songs", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getRecentSongs(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "not authenticated"));
        }

        List<Song> songs = songRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(5)
                .toList();

        List<Map<String, Object>> result = songs.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("title", s.getTitle());
            m.put("artist", s.getArtist() != null ? s.getArtist().getName() : "-");
            m.put("duration", s.getFormattedDuration());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/update", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @RequestParam(required = false) String newUsername,
            @RequestParam(required = false) String favoriteGenre,
            @RequestParam(required = false) String favoriteArtist,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "not authenticated"));
        }

        try {
            User user = userRepository
                    .findByUsername(userDetails.getUsername())
                    .orElseThrow();

            if (newUsername != null && !newUsername.isBlank()) {
                String trimmed = newUsername.trim();
                if (!trimmed.equals(user.getUsername())) {
                    Optional<User> existing = userRepository.findByUsername(trimmed);
                    if (existing.isPresent()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Это имя уже занято"));
                    }
                    user.setUsername(trimmed);
                }
            }

            if (favoriteGenre != null) user.setFavoriteGenre(favoriteGenre);
            if (favoriteArtist != null) user.setFavoriteArtist(favoriteArtist);

            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    String base64 = Base64.getEncoder().encodeToString(avatarFile.getBytes());
                    String mimeType = avatarFile.getContentType();
                    if (mimeType == null || mimeType.isBlank()) {
                        mimeType = "image/jpeg";
                    }
                    user.setAvatarBase64("data:" + mimeType + ";base64," + base64);
                } catch (IOException e) {
                    return ResponseEntity.status(500)
                            .body(Map.of("error", "Avatar upload failed"));
                }
            }

            if (newPassword != null && !newPassword.isBlank()) {
                if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Неверный старый пароль"));
                }
                if (!newPassword.equals(confirmPassword)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Пароли не совпадают"));
                }
                if (newPassword.length() < 8) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Пароль минимум 8 символов"));
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            userRepository.save(user);

            UserDetails updatedDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            updatedDetails, null, updatedDetails.getAuthorities());
            newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(newAuth);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", user.getUsername(),
                    "avatarBase64", user.getAvatarBase64() != null ? user.getAvatarBase64() : ""
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Server error"));
        }
    }
}
