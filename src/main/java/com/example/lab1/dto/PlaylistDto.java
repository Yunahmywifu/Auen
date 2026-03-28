package com.example.lab1.dto;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private int trackCount;
    private List<TrackDto> tracks = new ArrayList<>();

    public PlaylistDto() {}

    public PlaylistDto(String id, String name, String description, String imageUrl, int trackCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.trackCount = trackCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public List<TrackDto> getTracks() { return tracks; }
    public void setTracks(List<TrackDto> tracks) { this.tracks = tracks; }
}

