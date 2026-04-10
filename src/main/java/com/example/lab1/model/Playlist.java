package com.example.lab1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String coverImage;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isPublic = true;

    @Column
    private String shareToken;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "playlist_song",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @JsonIgnoreProperties({"playlists"})
    private List<Song> songs = new ArrayList<>();

    public Playlist() {}

    public Playlist(String name, String description, String createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }

    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }

    public void addSong(Song song) {
        this.songs.add(song);
    }

    public void removeSong(Long songId) {
        this.songs.removeIf(s -> s.getId().equals(songId));
    }

    public int getTotalDuration() {
        return songs.stream().mapToInt(Song::getDuration).sum();
    }

    public void generateShareToken() {
        this.shareToken = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public String getFormattedTotalDuration() {
        int total = getTotalDuration();
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;
        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        }
        return String.format("%dm %02ds", minutes, seconds);
    }
}
