package com.example.lab1.controller;

import com.example.lab1.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search/tracks")
    public ResponseEntity<?> searchTracks(@RequestParam String q) {
        try {
            Track[] tracks = spotifyService.searchTracks(q);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Track track : tracks) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", track.getName());
                item.put("artist", track.getArtists() != null && track.getArtists().length > 0
                        ? track.getArtists()[0].getName() : "");
                item.put("album", track.getAlbum() != null ? track.getAlbum().getName() : "");
                item.put("duration", track.getDurationMs() / 1000);
                item.put("genre", "");
                item.put("preview", track.getPreviewUrl());

                if (track.getAlbum() != null && track.getAlbum().getImages() != null
                        && track.getAlbum().getImages().length > 0) {
                    item.put("image", track.getAlbum().getImages()[0].getUrl());
                } else {
                    item.put("image", "");
                }
                result.add(item);
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Spotify error"));
        }
    }

    @GetMapping("/charts/top50")
    public ResponseEntity<List<Map<String, Object>>> getTop50Charts() {
        try {
            return ResponseEntity.ok(spotifyService.getTop20Charts());
        } catch (Exception e) {
            System.out.println("❌ /charts/top50 error: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/charts/artists")
    public ResponseEntity<List<Map<String, Object>>> getTopArtists() {
        try {
            return ResponseEntity.ok(spotifyService.getTopArtists());
        } catch (Exception e) {
            System.out.println("❌ /charts/artists error: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/search/artists")
    public ResponseEntity<?> searchArtists(@RequestParam String q) {
        try {
            Artist[] artists = spotifyService.searchArtists(q);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Artist artist : artists) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", artist.getName());
                String[] genres = artist.getGenres();
                item.put("genre", genres != null && genres.length > 0 ? genres[0] : "");
                item.put("followers", artist.getFollowers() != null ? artist.getFollowers().getTotal() : 0);

                if (artist.getImages() != null && artist.getImages().length > 0) {
                    item.put("image", artist.getImages()[0].getUrl());
                } else {
                    item.put("image", "");
                }
                result.add(item);
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Spotify error"));
        }
    }
}