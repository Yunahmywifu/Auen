package com.example.lab1.controller;

import com.example.lab1.model.Song;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/songs")
public class SongRestController {

    private static List<Song> songs = new ArrayList<>();
    private static int nextId = 1;

    static {
        songs.add(new Song(nextId++, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock"));
        songs.add(new Song(nextId++, "Imagine", "John Lennon", "Imagine", 183, "Pop"));
        songs.add(new Song(nextId++, "Smells Like Teen Spirit", "Nirvana", "Nevermind", 301, "Grunge"));
        songs.add(new Song(nextId++, "Billie Jean", "Michael Jackson", "Thriller", 294, "Pop"));
        songs.add(new Song(nextId++, "Hotel California", "Eagles", "Hotel California", 391, "Rock"));
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable int id) {
        Optional<Song> song = songs.stream()
                .filter(s -> s.getId() == id)
                .findFirst();
        return song.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Song> addSong(@RequestBody Song song) {
        song.setId(nextId++);
        songs.add(song);
        return ResponseEntity.status(HttpStatus.CREATED).body(song);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable int id, @RequestBody Song updatedSong) {
        Optional<Song> existingSong = songs.stream()
                .filter(s -> s.getId() == id)
                .findFirst();

        if (existingSong.isPresent()) {
            Song song = existingSong.get();
            song.setTitle(updatedSong.getTitle());
            song.setArtist(updatedSong.getArtist());
            song.setAlbum(updatedSong.getAlbum());
            song.setDuration(updatedSong.getDuration());
            song.setGenre(updatedSong.getGenre());
            return ResponseEntity.ok(song);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable int id) {
        boolean removed = songs.removeIf(s -> s.getId() == id);
        if (removed) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    public static List<Song> getAllSongsList() {
        return new ArrayList<>(songs);
    }
}



