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

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

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
        var request = spotifyApi.clientCredentials().build();
        var credentials = request.execute();
        spotifyApi.setAccessToken(credentials.getAccessToken());
        System.out.println("✅ Spotify token OK, expires in: "
                + credentials.getExpiresIn() + "s");
    }

    // 🔍 Поиск треков
    public Track[] searchTracks(String query) throws IOException, SpotifyWebApiException, ParseException {
        try {
            return spotifyApi.searchTracks(query)
                    .limit(8)
                    .build()
                    .execute()
                    .getItems();
        } catch (SpotifyWebApiException e) {
            // Токен истёк — обновляем и повторяем
            refreshToken();
            return spotifyApi.searchTracks(query)
                    .limit(8)
                    .build()
                    .execute()
                    .getItems();
        }
    }

    // 🎤 Поиск артистов
    public Artist[] searchArtists(String query) throws IOException, SpotifyWebApiException, ParseException {
        try {
            return spotifyApi.searchArtists(query)
                    .limit(8)
                    .build()
                    .execute()
                    .getItems();
        } catch (SpotifyWebApiException e) {
            // Токен истёк — обновляем и повторяем
            refreshToken();
            return spotifyApi.searchArtists(query)
                    .limit(8)
                    .build()
                    .execute()
                    .getItems();
        }
    }
}