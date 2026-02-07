package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/artists")
public class ArtistController {

    private static List<Artist> artists = new ArrayList<>();
    private static int nextId = 1;

    static {
        artists.add(new Artist(nextId++, "Queen", "United Kingdom", "Rock", 1970));
        artists.add(new Artist(nextId++, "The Beatles", "United Kingdom", "Rock/Pop", 1960));
        artists.add(new Artist(nextId++, "Michael Jackson", "USA", "Pop", 1964));
        artists.add(new Artist(nextId++, "Nirvana", "USA", "Grunge", 1987));
        artists.add(new Artist(nextId++, "Pink Floyd", "United Kingdom", "Progressive Rock", 1965));
    }

    @GetMapping
    public String listArtists(Model model) {
        model.addAttribute("artists", artists);
        return "artists";
    }

    @PostMapping
    public String addArtist(@RequestParam String name,
                            @RequestParam String country,
                            @RequestParam String genre,
                            @RequestParam int yearFounded) {
        Artist artist = new Artist(nextId++, name, country, genre, yearFounded);
        artists.add(artist);
        return "redirect:/artists";
    }

    @GetMapping("/delete/{id}")
    public String deleteArtist(@PathVariable int id) {
        artists.removeIf(a -> a.getId() == id);
        return "redirect:/artists";
    }
}

