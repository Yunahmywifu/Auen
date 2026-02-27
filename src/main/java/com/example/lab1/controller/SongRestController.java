package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import com.example.lab1.model.Song;
import com.example.lab1.repository.ArtistRepository;
import com.example.lab1.repository.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
public class SongRestController {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public SongRestController(SongRepository songRepository,
                               ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String album) {
        List<Song> songs = songRepository.findAll();
        if (genre != null) {
            songs = songs.stream()
                    .filter(s -> s.getGenre() != null && s.getGenre().toLowerCase().contains(genre.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (album != null) {
            songs = songs.stream()
                    .filter(s -> s.getAlbum() != null && s.getAlbum().toLowerCase().contains(album.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        return songRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Song> addSong(@RequestBody Song song) {
        if (song.getArtist() != null && song.getArtist().getId() != null) {
            Artist artist = artistRepository.findById(song.getArtist().getId())
                    .orElse(null);
            song.setArtist(artist);
        }
        Song saved = songRepository.save(song);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable Long id,
                                            @RequestBody Song updated) {
        return songRepository.findById(id).map(song -> {
            song.setTitle(updated.getTitle());
            song.setAlbum(updated.getAlbum());
            song.setDuration(updated.getDuration());
            song.setGenre(updated.getGenre());
            if (updated.getArtist() != null && updated.getArtist().getId() != null) {
                artistRepository.findById(updated.getArtist().getId())
                        .ifPresent(song::setArtist);
            }
            return ResponseEntity.ok(songRepository.save(song));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        if (!songRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        songRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-artist/{artistId}")
    public ResponseEntity<List<Song>> getSongsByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(songRepository.findByArtistId(artistId));
    }
}
