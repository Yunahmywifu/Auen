package com.example.lab1.service;

import jakarta.annotation.PostConstruct;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.*;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    public SpotifyApi getSpotifyApi() { return spotifyApi; }

    // Вызывается автоматически при старте
    @PostConstruct
    public void init() {
        try {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .build();
            refreshToken();
        } catch (Exception e) {
            System.out.println("Spotify init error: " + e.getMessage());
        }
    }

    // Получаем токен (действует 1 час)
    public void refreshToken() throws IOException, SpotifyWebApiException, ParseException {
        var credentials = spotifyApi.clientCredentials().build().execute();
        spotifyApi.setAccessToken(credentials.getAccessToken());
        System.out.println("✅ Spotify token OK, expires in: " + credentials.getExpiresIn() + "s");
    }

    // ── Поиск треков (limit 8 для search-bar) ──────────────────
    public Track[] searchTracks(String query) throws IOException, SpotifyWebApiException, ParseException {
        try {
            return spotifyApi.searchTracks(query).limit(8).build().execute().getItems();
        } catch (SpotifyWebApiException e) {
            refreshToken();
            return spotifyApi.searchTracks(query).limit(8).build().execute().getItems();
        }
    }

    // ── Поиск артистов (limit 8 для search-bar) ───────────────
    public Artist[] searchArtists(String query) throws IOException, SpotifyWebApiException, ParseException {
        try {
            return spotifyApi.searchArtists(query).limit(8).build().execute().getItems();
        } catch (SpotifyWebApiException e) {
            refreshToken();
            return spotifyApi.searchArtists(query).limit(8).build().execute().getItems();
        }
    }

    // ── Top 50 Charts ──────────────────────────────────────────
    public List<Map<String, Object>> getTop20Charts() {
        String[] queries = {"top hits 2024", "pop hits", "hip hop hits"};
        Map<String, Map<String, Object>> unique = new LinkedHashMap<>();

        for (String q : queries) {
            try {
                Track[] tracks = spotifyApi.searchTracks(q).limit(10).build().execute().getItems();
                for (Track t : tracks) {
                    String key = t.getName().toLowerCase() + "|"
                            + (t.getArtists() != null && t.getArtists().length > 0
                               ? t.getArtists()[0].getName().toLowerCase() : "");
                    if (unique.containsKey(key)) continue;
                    Map<String, Object> item = new HashMap<>();
                    item.put("title",      t.getName());
                    item.put("artist",     t.getArtists() != null && t.getArtists().length > 0
                                           ? t.getArtists()[0].getName() : "Unknown");
                    item.put("album",      t.getAlbum() != null ? t.getAlbum().getName() : "");
                    item.put("duration",   t.getDurationMs() != null ? t.getDurationMs() / 1000 : 0);
                    item.put("popularity", t.getPopularity() != null ? t.getPopularity() : 0);
                    item.put("preview",    t.getPreviewUrl() != null ? t.getPreviewUrl() : "");
                    String img = "";
                    if (t.getAlbum() != null && t.getAlbum().getImages() != null
                            && t.getAlbum().getImages().length > 0) {
                        img = t.getAlbum().getImages()[0].getUrl();
                    }
                    item.put("image", img);
                    unique.put(key, item);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Skipping query [" + q + "]: " + e.getMessage());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(unique.values());
        result.sort((a, b) -> Integer.compare(
                (int) b.getOrDefault("popularity", 0),
                (int) a.getOrDefault("popularity", 0)));
        return result.size() > 20 ? result.subList(0, 20) : result;
    }

    public List<Map<String, Object>> getTopArtists() {
        String[] queries = {"pop artist", "rock artist", "hip hop artist", "electronic artist", "rnb artist"};
        Map<String, Map<String, Object>> unique = new LinkedHashMap<>();

        for (String q : queries) {
            try {
                Artist[] artists = spotifyApi.searchArtists(q).limit(10).build().execute().getItems();
                for (Artist a : artists) {
                    String key = a.getName().toLowerCase();
                    if (unique.containsKey(key)) continue;
                    Map<String, Object> item = new HashMap<>();
                    item.put("name",       a.getName());
                    item.put("genre",      a.getGenres() != null && a.getGenres().length > 0
                                           ? a.getGenres()[0] : "Music");
                    item.put("followers",  a.getFollowers() != null ? a.getFollowers().getTotal() : 0);
                    item.put("popularity", a.getPopularity() != null ? a.getPopularity() : 0);
                    item.put("image",      a.getImages() != null && a.getImages().length > 0
                                           ? a.getImages()[0].getUrl() : "");
                    unique.put(key, item);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Skipping query [" + q + "]: " + e.getMessage());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(unique.values());
        result.sort((a, b) -> Integer.compare(
                (int) b.getOrDefault("followers", 0),
                (int) a.getOrDefault("followers", 0)));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }
}