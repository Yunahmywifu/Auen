package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import com.example.lab1.repository.ArtistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artists")
public class ArtistRestController {

    private final ArtistRepository artistRepository;

    public ArtistRestController(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @GetMapping
    public ResponseEntity<List<Artist>> getAllArtists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country) {
        List<Artist> artists = artistRepository.findAll();
        if (name != null) {
            artists = artists.stream()
                    .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (country != null) {
            artists = artists.stream()
                    .filter(a -> a.getCountry() != null && a.getCountry().toLowerCase().contains(country.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long id) {
        return artistRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Artist> addArtist(@RequestBody Artist artist) {
        Artist saved = artistRepository.save(artist);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable Long id,
                                                @RequestBody Artist updated) {
        return artistRepository.findById(id).map(artist -> {
            artist.setName(updated.getName());
            artist.setCountry(updated.getCountry());
            artist.setGenre(updated.getGenre());
            artist.setYearFounded(updated.getYearFounded());
            return ResponseEntity.ok(artistRepository.save(artist));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        if (!artistRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        artistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
