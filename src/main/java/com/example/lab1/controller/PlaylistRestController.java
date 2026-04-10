package com.example.lab1.controller;

import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import com.example.lab1.repository.PlaylistRepository;
import com.example.lab1.repository.SongRepository;
import com.example.lab1.service.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistRestController {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final SpotifyService spotifyService;

    public PlaylistRestController(PlaylistRepository playlistRepository,
                                   SongRepository songRepository,
                                   SpotifyService spotifyService) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.spotifyService = spotifyService;
    }

    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String createdBy) {
        List<Playlist> playlists = playlistRepository.findAll();
        if (name != null) {
            playlists = playlists.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (createdBy != null) {
            playlists = playlists.stream()
                    .filter(p -> p.getCreatedBy() != null && p.getCreatedBy().toLowerCase().contains(createdBy.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable Long id) {
        return playlistRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Playlist> addPlaylist(@RequestBody Playlist playlist) {
        if (playlist.getSongs() == null) {
            playlist.setSongs(List.of());
        }
        if (playlist.getShareToken() == null || playlist.getShareToken().isBlank()) {
            playlist.generateShareToken();
        }
        Playlist saved = playlistRepository.save(playlist);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable Long id,
                                                    @RequestBody Playlist updated) {
        return playlistRepository.findById(id).map(playlist -> {
            playlist.setName(updated.getName());
            playlist.setDescription(updated.getDescription());
            playlist.setCreatedBy(updated.getCreatedBy());
            return ResponseEntity.ok(playlistRepository.save(playlist));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        if (!playlistRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        playlistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Playlist> addSongToPlaylist(@PathVariable Long playlistId,
                                                       @PathVariable Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElse(null);
        Song song = songRepository.findById(songId).orElse(null);

        if (playlist == null || song == null) {
            return ResponseEntity.notFound().build();
        }

        if (!playlist.getSongs().contains(song)) {
            playlist.addSong(song);
            playlistRepository.save(playlist);
        }
        return ResponseEntity.ok(playlist);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Playlist> removeSongFromPlaylist(@PathVariable Long playlistId,
                                                            @PathVariable Long songId) {
        return playlistRepository.findById(playlistId).map(playlist -> {
            playlist.removeSong(songId);
            return ResponseEntity.ok(playlistRepository.save(playlist));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<Song>> getPlaylistSongs(@PathVariable Long playlistId) {
        return playlistRepository.findById(playlistId)
                .map(p -> ResponseEntity.ok(p.getSongs()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get shared playlist by token (public access)
    @GetMapping("/share/{token}")
    public ResponseEntity<?> getSharedPlaylist(@PathVariable String token) {
        return playlistRepository.findByShareToken(token)
                .filter(Playlist::isPublic)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Toggle public/private
    @PostMapping("/{id}/toggle-visibility")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id) {
        return playlistRepository.findById(id).map(playlist -> {
            playlist.setPublic(!playlist.isPublic());
            if (playlist.getShareToken() == null || playlist.getShareToken().isBlank()) {
                playlist.generateShareToken();
            }
            playlistRepository.save(playlist);
            return ResponseEntity.ok(Map.of(
                    "isPublic", playlist.isPublic(),
                    "shareToken", playlist.getShareToken()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Update playlist cover image
    @PostMapping("/{id}/cover")
    public ResponseEntity<?> updateCover(@PathVariable Long id,
                                         @RequestParam(required = false) MultipartFile coverFile,
                                         @RequestParam(required = false) String coverUrl) {
        return playlistRepository.findById(id).map(playlist -> {
            try {
                if (coverFile != null && !coverFile.isEmpty()) {
                    byte[] bytes = coverFile.getBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String mime = coverFile.getContentType() != null ? coverFile.getContentType() : "image/jpeg";
                    playlist.setCoverImage("data:" + mime + ";base64," + base64);
                } else if (coverUrl != null && !coverUrl.isBlank()) {
                    playlist.setCoverImage(coverUrl);
                }
                playlistRepository.save(playlist);
                return ResponseEntity.ok(Map.of("coverImage", playlist.getCoverImage() != null ? playlist.getCoverImage() : ""));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "cover update error"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Update playlist info (name, description)
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updatePlaylistInfo(@PathVariable Long id,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String description) {
        return playlistRepository.findById(id).map(playlist -> {
            if (name != null && !name.isBlank()) playlist.setName(name);
            if (description != null) playlist.setDescription(description);
            playlistRepository.save(playlist);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Reorder songs in playlist
    @PostMapping("/{id}/reorder")
    public ResponseEntity<?> reorderSongs(@PathVariable Long id,
                                          @RequestBody List<Long> songIds) {
        return playlistRepository.findById(id).map(playlist -> {
            List<Song> reordered = songIds.stream()
                    .map(songId -> playlist.getSongs().stream()
                            .filter(s -> s.getId().equals(songId))
                            .findFirst().orElse(null))
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
            playlist.setSongs(reordered);
            playlistRepository.save(playlist);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Auto-playlist by genre
    @PostMapping("/{id}/auto-fill-genre")
    public ResponseEntity<?> autoFillByGenre(@PathVariable Long id,
                                             @RequestParam String genre) {
        return playlistRepository.findById(id).map(playlist -> {
            List<Song> genreSongs = songRepository.findAll().stream()
                    .filter(s -> s.getGenre() != null &&
                            s.getGenre().toLowerCase().contains(genre.toLowerCase()))
                    .filter(s -> !playlist.getSongs().contains(s))
                    .collect(Collectors.toList());
            genreSongs.forEach(playlist::addSong);
            playlistRepository.save(playlist);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "added", genreSongs.size()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Shuffle songs in playlist
    @PostMapping("/{id}/shuffle")
    public ResponseEntity<?> shufflePlaylist(@PathVariable Long id) {
        return playlistRepository.findById(id).map(playlist -> {
            List<Song> songs = new ArrayList<>(playlist.getSongs());
            Collections.shuffle(songs);
            playlist.setSongs(songs);
            playlistRepository.save(playlist);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Spotify recommendations based on playlist genres
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable Long id) {
        return playlistRepository.findById(id).map(playlist -> {
            String genre = playlist.getSongs().stream()
                    .filter(s -> s.getGenre() != null && !s.getGenre().isEmpty())
                    .collect(Collectors.groupingBy(Song::getGenre, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("pop");

            try {
                se.michaelthelin.spotify.model_objects.specification.Track[] tracks =
                        spotifyService.searchTracks("genre:" + genre + " year:2024");

                List<Map<String, Object>> result = new ArrayList<>();
                for (var t : tracks) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", t.getName());
                    item.put("artist", t.getArtists() != null && t.getArtists().length > 0
                            ? t.getArtists()[0].getName() : "");
                    item.put("album", t.getAlbum() != null ? t.getAlbum().getName() : "");
                    item.put("duration", t.getDurationMs() / 1000);
                    item.put("preview", t.getPreviewUrl() != null ? t.getPreviewUrl() : "");
                    item.put("image", t.getAlbum() != null &&
                            t.getAlbum().getImages() != null &&
                            t.getAlbum().getImages().length > 0
                            ? t.getAlbum().getImages()[0].getUrl() : "");
                    result.add(item);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.ok(new ArrayList<>());
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
