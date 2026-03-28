package com.example.lab1.dto;

import java.util.ArrayList;
import java.util.List;

public class ArtistDto {
    private String id;
    private String name;
    private String genre;
    private String imageUrl;
    private int followers;
    private List<TrackDto> topTracks = new ArrayList<>();

    public ArtistDto() {}

    public ArtistDto(String id, String name, String genre, String imageUrl, int followers) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.followers = followers;
    }

    public String getFormattedFollowers() {
        if (followers >= 1_000_000) {
            return String.format("%.1fM", followers / 1_000_000.0);
        } else if (followers >= 1_000) {
            return String.format("%.1fK", followers / 1_000.0);
        }
        return String.valueOf(followers);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public List<TrackDto> getTopTracks() { return topTracks; }
    public void setTopTracks(List<TrackDto> topTracks) { this.topTracks = topTracks; }
}

