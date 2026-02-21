package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/artists")
public class ArtistRestController {

    private static List<Artist> artists = new ArrayList<>();
    private static int nextId = 1;

    static {
        artists.add(new Artist(nextId++, "Queen", "United Kingdom", "Rock", 1970));
        artists.add(new Artist(nextId++, "The Beatles", "United Kingdom", "Rock/Pop", 1960));
        artists.add(new Artist(nextId++, "Michael Jackson", "USA", "Pop", 1964));
        artists.add(new Artist(nextId++, "Nirvana", "USA", "Grunge", 1987));
        artists.add(new Artist(nextId++, "Pink Floyd", "United Kingdom", "Progressive Rock", 1965));
    }

    @GetMapping
    public ResponseEntity<List<Artist>> getAllArtists() {
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable int id) {
        Optional<Artist> artist = artists.stream()
                .filter(a -> a.getId() == id)
                .findFirst();
        return artist.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Artist> addArtist(@RequestBody Artist artist) {
        artist.setId(nextId++);
        artists.add(artist);
        return ResponseEntity.status(HttpStatus.CREATED).body(artist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable int id, @RequestBody Artist updatedArtist) {
        Optional<Artist> existingArtist = artists.stream()
                .filter(a -> a.getId() == id)
                .findFirst();

        if (existingArtist.isPresent()) {
            Artist artist = existingArtist.get();
            artist.setName(updatedArtist.getName());
            artist.setCountry(updatedArtist.getCountry());
            artist.setGenre(updatedArtist.getGenre());
            artist.setYearFounded(updatedArtist.getYearFounded());
            return ResponseEntity.ok(artist);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable int id) {
        boolean removed = artists.removeIf(a -> a.getId() == id);
        if (removed) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

