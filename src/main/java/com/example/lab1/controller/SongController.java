package com.example.lab1.controller;

import com.example.lab1.model.Song;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/songs")
public class SongController {

    private static List<Song> songs = new ArrayList<>();
    private static int nextId = 1;

    static {
        songs.add(new Song(nextId++, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock"));
        songs.add(new Song(nextId++, "Imagine", "John Lennon", "Imagine", 183, "Pop"));
        songs.add(new Song(nextId++, "Smells Like Teen Spirit", "Nirvana", "Nevermind", 301, "Grunge"));
        songs.add(new Song(nextId++, "Billie Jean", "Michael Jackson", "Thriller", 294, "Pop"));
        songs.add(new Song(nextId++, "Hotel California", "Eagles", "Hotel California", 391, "Rock"));
    }

    @GetMapping
    public String listSongs(Model model) {
        model.addAttribute("songs", songs);
        return "songs";
    }

    @PostMapping
    public String addSong(@RequestParam String title,
                          @RequestParam String artist,
                          @RequestParam String album,
                          @RequestParam int duration,
                          @RequestParam String genre) {
        Song song = new Song(nextId++, title, artist, album, duration, genre);
        songs.add(song);
        return "redirect:/songs";
    }

    @GetMapping("/delete/{id}")
    public String deleteSong(@PathVariable int id) {
        songs.removeIf(s -> s.getId() == id);
        return "redirect:/songs";
    }

    public static List<Song> getAllSongs() {
        return new ArrayList<>(songs);
    }
}

