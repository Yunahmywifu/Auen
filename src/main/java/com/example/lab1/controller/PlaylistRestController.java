package com.example.lab1.controller;

import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import com.example.lab1.repository.PlaylistRepository;
import com.example.lab1.repository.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistRestController {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public PlaylistRestController(PlaylistRepository playlistRepository,
                                   SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
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
}
