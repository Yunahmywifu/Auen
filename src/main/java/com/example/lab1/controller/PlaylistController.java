package com.example.lab1.controller;

import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    private static List<Playlist> playlists = new ArrayList<>();
    private static int nextId = 1;

    static {
        Playlist rockPlaylist = new Playlist(nextId++, "Rock Classics", "Best rock songs of all time", "Admin");
        rockPlaylist.addSong(new Song(1, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock"));
        rockPlaylist.addSong(new Song(5, "Hotel California", "Eagles", "Hotel California", 391, "Rock"));
        playlists.add(rockPlaylist);

        Playlist popPlaylist = new Playlist(nextId++, "Pop Hits", "Popular pop songs", "User1");
        popPlaylist.addSong(new Song(2, "Imagine", "John Lennon", "Imagine", 183, "Pop"));
        popPlaylist.addSong(new Song(4, "Billie Jean", "Michael Jackson", "Thriller", 294, "Pop"));
        playlists.add(popPlaylist);
    }

    @GetMapping
    public String listPlaylists(Model model) {
        model.addAttribute("playlists", playlists);
        model.addAttribute("availableSongs", SongController.getAllSongs());
        return "playlists";
    }

    @PostMapping
    public String addPlaylist(@RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String createdBy) {
        Playlist playlist = new Playlist(nextId++, name, description, createdBy);
        playlists.add(playlist);
        return "redirect:/playlists";
    }

    @GetMapping("/delete/{id}")
    public String deletePlaylist(@PathVariable int id) {
        playlists.removeIf(p -> p.getId() == id);
        return "redirect:/playlists";
    }

    @PostMapping("/addSong")
    public String addSongToPlaylist(@RequestParam int playlistId,
                                     @RequestParam int songId) {
        Playlist playlist = findPlaylistById(playlistId);
        Song song = findSongById(songId);

        if (playlist != null && song != null) {
            playlist.addSong(song);
        }
        return "redirect:/playlists";
    }

    @GetMapping("/removeSong")
    public String removeSongFromPlaylist(@RequestParam int playlistId,
                                          @RequestParam int songId) {
        Playlist playlist = findPlaylistById(playlistId);
        if (playlist != null) {
            playlist.getSongs().removeIf(s -> s.getId() == songId);
        }
        return "redirect:/playlists";
    }

    private Playlist findPlaylistById(int id) {
        return playlists.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private Song findSongById(int id) {
        List<Song> allSongs = SongController.getAllSongs();
        return allSongs.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
