package com.example.lab1.service;

import com.example.lab1.dto.ArtistDto;
import com.example.lab1.dto.PlaylistDto;
import com.example.lab1.dto.TrackDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyHomeService {

    private final SpotifyService spotifyService;

    private static final String[][] GENRE_PLAYLISTS = {
        {"Pop Hits 2025",       "genre:pop year:2025"},
        {"Hip-Hop Vibes",       "genre:hip-hop year:2025"},
        {"Rock Classics",       "genre:rock year:2024"},
        {"K-Pop Global",        "genre:k-pop year:2025"},
        {"R&B Soul",            "genre:r-b year:2025"},
        {"Electronic Beats",    "genre:electronic year:2025"},
        {"Latin Hits",          "genre:latin year:2025"},
        {"Indie Chill",         "genre:indie year:2025"},
    };

    private static final String[] TOP_ARTIST_QUERIES = {
        "Taylor Swift", "Drake", "The Weeknd", "Billie Eilish",
        "Bad Bunny", "BTS", "Olivia Rodrigo", "Doja Cat",
        "Harry Styles", "Dua Lipa"
    };

    public SpotifyHomeService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    private SpotifyApi getApi() {
        return spotifyService.getSpotifyApi();
    }

    // ─────────────────────────────────────────────────
    // Featured Playlists — genre-based search playlists
    // ─────────────────────────────────────────────────
    @Cacheable(value = "spotifyFeatured", key = "'featured'")
    public List<PlaylistDto> getFeaturedPlaylists() {
        List<PlaylistDto> result = new ArrayList<>();
        try {
            for (String[] genrePlaylist : GENRE_PLAYLISTS) {
                String name  = genrePlaylist[0];
                String query = genrePlaylist[1];
                try {
                    Track[] tracks = getApi().searchTracks(query)
                            .limit(10)
                            .build()
                            .execute()
                            .getItems();

                    PlaylistDto dto = new PlaylistDto();
                    dto.setId(name.replaceAll("\\s+", "-").toLowerCase());
                    dto.setName(name);
                    dto.setDescription(tracks.length + " треков");
                    dto.setTrackCount(tracks.length);

                    // Обложка — первый трек с изображением
                    dto.setImageUrl("");
                    for (Track t : tracks) {
                        if (t.getAlbum() != null && t.getAlbum().getImages() != null
                                && t.getAlbum().getImages().length > 0) {
                            dto.setImageUrl(t.getAlbum().getImages()[0].getUrl());
                            break;
                        }
                    }

                    // Треки плейлиста
                    List<TrackDto> trackDtos = new ArrayList<>();
                    for (Track t : tracks) {
                        trackDtos.add(toTrackDto(t));
                    }
                    dto.setTracks(trackDtos);
                    result.add(dto);
                } catch (Exception ex) {
                    System.out.println("⚠️ Playlist search failed for: " + name + " — " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ getFeaturedPlaylists error: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────
    // Top 10 Artists — search by artist name
    // ─────────────────────────────────────────────────
    @Cacheable(value = "spotifyTopArtists", key = "'topArtists'")
    public List<ArtistDto> getTopArtists() {
        List<ArtistDto> result = new ArrayList<>();
        try {
            for (String artistName : TOP_ARTIST_QUERIES) {
                try {
                    Artist[] artists = getApi().searchArtists(artistName)
                            .limit(1)
                            .build()
                            .execute()
                            .getItems();

                    if (artists == null || artists.length == 0) continue;
                    Artist artist = artists[0];

                    ArtistDto dto = new ArtistDto();
                    dto.setId(artist.getId());
                    dto.setName(artist.getName());

                    String[] genres = artist.getGenres();
                    dto.setGenre(genres != null && genres.length > 0 ? capitalize(genres[0]) : "Pop");

                    if (artist.getImages() != null && artist.getImages().length > 0) {
                        dto.setImageUrl(artist.getImages()[0].getUrl());
                    } else {
                        dto.setImageUrl("");
                    }
                    dto.setFollowers(artist.getFollowers() != null ? artist.getFollowers().getTotal() : 0);

                    // Топ треки артиста через search
                    try {
                        Track[] tracks = getApi().searchTracks("artist:\"" + artist.getName() + "\"")
                                .limit(5)
                                .build()
                                .execute()
                                .getItems();

                        List<TrackDto> trackDtos = new ArrayList<>();
                        for (Track t : tracks) {
                            trackDtos.add(toTrackDto(t));
                        }
                        dto.setTopTracks(trackDtos);
                    } catch (Exception ex) {
                        dto.setTopTracks(new ArrayList<>());
                    }

                    result.add(dto);
                } catch (Exception ex) {
                    System.out.println("⚠️ Artist search failed for: " + artistName);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ getTopArtists error: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────
    // Top 50 Tracks — search popular tracks 2025
    // ─────────────────────────────────────────────────
    @Cacheable(value = "spotifyTop50", key = "'top50'")
    public List<TrackDto> getTop50Tracks() {
        List<TrackDto> result = new ArrayList<>();
        try {
            // Запрашиваем популярные треки двумя запросами (лимит 50 у Spotify)
            String[] queries = {"year:2025 tag:hipster", "year:2025 genre:pop"};
            for (String q : queries) {
                try {
                    Track[] tracks = getApi().searchTracks(q)
                            .limit(25)
                            .build()
                            .execute()
                            .getItems();
                    for (Track t : tracks) {
                        result.add(toTrackDto(t));
                    }
                } catch (Exception ex) {
                    System.out.println("⚠️ Top50 search failed: " + ex.getMessage());
                }
                if (result.size() >= 50) break;
            }
            // Сортируем по popularity убыванию
            result.sort((a, b) -> Integer.compare(b.getPopularity(), a.getPopularity()));
            if (result.size() > 50) result = result.subList(0, 50);
        } catch (Exception e) {
            System.out.println("❌ getTop50Tracks error: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────
    // Helper — Track → TrackDto
    // ─────────────────────────────────────────────────
    private TrackDto toTrackDto(Track t) {
        TrackDto dto = new TrackDto();
        dto.setId(t.getId());
        dto.setTitle(t.getName() != null ? t.getName() : "");
        dto.setArtistName(t.getArtists() != null && t.getArtists().length > 0
                ? t.getArtists()[0].getName() : "");
        dto.setAlbum(t.getAlbum() != null ? t.getAlbum().getName() : "");
        dto.setDuration(t.getDurationMs() != null ? t.getDurationMs() / 1000 : 0);
        dto.setPopularity(t.getPopularity());
        dto.setGenre("");
        dto.setPreviewUrl(t.getPreviewUrl());
        if (t.getAlbum() != null && t.getAlbum().getImages() != null
                && t.getAlbum().getImages().length > 0) {
            dto.setImageUrl(t.getAlbum().getImages()[0].getUrl());
        } else {
            dto.setImageUrl("");
        }
        return dto;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

