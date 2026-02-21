package com.example.lab1.controller;

import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistRestController {

    private static List<Playlist> playlists = new ArrayList<>();
    private static int nextId = 1;

    static {
        Playlist rockPlaylist = new Playlist(nextId++, "Rock Classics", "Best rock songs of all time", "Admin");
        rockPlaylist.addSong(new Song(1, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock"));
        rockPlaylist.addSong(new Song(5, "Hotel California", "Eagles", "Hotel California", 391, "Rock"));
        playlists.add(rockPlaylist);

        Playlist popPlaylist = new Playlist(nextId++, "Pop Hits", "Popular pop songs", "User1");
        popPlaylist.addSong(new Song(2, "Imagine", "John Lennon", "Imagine", 183, "Pop"));
        popPlaylist.addSong(new Song(4, "Billie Jean", "Michael Jackson", "Thriller", 294, "Pop"));
        playlists.add(popPlaylist);
    }

    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable int id) {
        Optional<Playlist> playlist = playlists.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
        return playlist.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Playlist> addPlaylist(@RequestBody Playlist playlist) {
        playlist.setId(nextId++);
        if (playlist.getSongs() == null) {
            playlist.setSongs(new ArrayList<>());
        }
        playlists.add(playlist);
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable int id, @RequestBody Playlist updatedPlaylist) {
        Optional<Playlist> existingPlaylist = playlists.stream()
                .filter(p -> p.getId() == id)
                .findFirst();

        if (existingPlaylist.isPresent()) {
            Playlist playlist = existingPlaylist.get();
            playlist.setName(updatedPlaylist.getName());
            playlist.setDescription(updatedPlaylist.getDescription());
            playlist.setCreatedBy(updatedPlaylist.getCreatedBy());
            return ResponseEntity.ok(playlist);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable int id) {
        boolean removed = playlists.removeIf(p -> p.getId() == id);
        if (removed) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<Playlist> addSongToPlaylist(@PathVariable int playlistId,
                                                       @RequestBody Song song) {
        Optional<Playlist> playlistOpt = playlists.stream()
                .filter(p -> p.getId() == playlistId)
                .findFirst();

        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            playlist.addSong(song);
            return ResponseEntity.ok(playlist);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Playlist> removeSongFromPlaylist(@PathVariable int playlistId,
                                                            @PathVariable int songId) {
        Optional<Playlist> playlistOpt = playlists.stream()
                .filter(p -> p.getId() == playlistId)
                .findFirst();

        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            playlist.getSongs().removeIf(s -> s.getId() == songId);
            return ResponseEntity.ok(playlist);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<Song>> getPlaylistSongs(@PathVariable int playlistId) {
        Optional<Playlist> playlistOpt = playlists.stream()
                .filter(p -> p.getId() == playlistId)
                .findFirst();

        return playlistOpt.map(playlist -> ResponseEntity.ok(playlist.getSongs()))
                .orElse(ResponseEntity.notFound().build());
    }
}

