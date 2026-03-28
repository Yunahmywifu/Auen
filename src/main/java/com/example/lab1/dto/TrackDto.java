package com.example.lab1.dto;

public class TrackDto {
    private String id;
    private String title;
    private String artistName;
    private String album;
    private String imageUrl;
    private String genre;
    private int duration;
    private int popularity;
    private String previewUrl;

    public TrackDto() {}

    public TrackDto(String id, String title, String artistName, String album,
                    String imageUrl, String genre, int duration, int popularity, String previewUrl) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.album = album;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.duration = duration;
        this.popularity = popularity;
        this.previewUrl = previewUrl;
    }

    public String getFormattedDuration() {
        return duration / 60 + ":" + String.format("%02d", duration % 60);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
}

