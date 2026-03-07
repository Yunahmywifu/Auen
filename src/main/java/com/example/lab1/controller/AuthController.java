package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import com.example.lab1.model.User;
import com.example.lab1.repository.ArtistRepository;
import com.example.lab1.repository.PlaylistRepository;
import com.example.lab1.repository.SongRepository;
import com.example.lab1.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          ArtistRepository artistRepository,
                          SongRepository songRepository,
                          PlaylistRepository playlistRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
    }


    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                                @RequestParam String password,
                                Model model) {
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Пользователь с таким именем уже существует!");
            return "register";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);
        return "redirect:/login?registered";
    }


    @GetMapping({"/", "/index"})
    public String indexPage() { return "index"; }

    @GetMapping("/artists")
    public String artistsPage(Model model) {
        model.addAttribute("artists", artistRepository.findAll());
        return "artists";
    }

    @GetMapping("/songs")
    public String songsPage(Model model) {
        model.addAttribute("songs", songRepository.findAll());
        model.addAttribute("artists", artistRepository.findAll());
        return "songs";
    }

    @GetMapping("/playlists")
    public String playlistsPage(Model model) {
        model.addAttribute("playlists", playlistRepository.findAll());
        model.addAttribute("availableSongs", songRepository.findAll());
        return "playlists";
    }


    @PostMapping("/artists")
    public String addArtist(@RequestParam String name,
                             @RequestParam String country,
                             @RequestParam String genre,
                             @RequestParam int yearFounded) {
        artistRepository.save(new Artist(name, country, genre, yearFounded));
        return "redirect:/artists";
    }

    @GetMapping("/artists/delete/{id}")
    public String deleteArtist(@PathVariable Long id) {
        artistRepository.deleteById(id);
        return "redirect:/artists";
    }


    @PostMapping("/songs")
    public String addSong(@RequestParam String title,
                           @RequestParam String album,
                           @RequestParam String genre,
                           @RequestParam int duration,
                           @RequestParam(required = false) Long artistId) {
        Song song = new Song();
        song.setTitle(title);
        song.setAlbum(album);
        song.setGenre(genre);
        song.setDuration(duration);
        if (artistId != null) {
            artistRepository.findById(artistId).ifPresent(song::setArtist);
        }
        songRepository.save(song);
        return "redirect:/songs";
    }

    @GetMapping("/songs/delete/{id}")
    public String deleteSong(@PathVariable Long id) {
        songRepository.deleteById(id);
        return "redirect:/songs";
    }


    @PostMapping("/playlists")
    public String addPlaylist(@RequestParam String name,
                               @RequestParam String description,
                               @RequestParam String createdBy) {
        playlistRepository.save(new Playlist(name, description, createdBy));
        return "redirect:/playlists";
    }

    @GetMapping("/playlists/delete/{id}")
    public String deletePlaylist(@PathVariable Long id) {
        playlistRepository.deleteById(id);
        return "redirect:/playlists";
    }

    @PostMapping("/playlists/addSong")
    public String addSongToPlaylist(@RequestParam Long playlistId,
                                     @RequestParam Long songId) {
        playlistRepository.findById(playlistId).ifPresent(playlist ->
            songRepository.findById(songId).ifPresent(song -> {
                if (!playlist.getSongs().contains(song)) {
                    playlist.addSong(song);
                    playlistRepository.save(playlist);
                }
            })
        );
        return "redirect:/playlists";
    }

    @GetMapping("/playlists/removeSong")
    public String removeSongFromPlaylist(@RequestParam Long playlistId,
                                          @RequestParam Long songId) {
        playlistRepository.findById(playlistId).ifPresent(playlist -> {
            playlist.removeSong(songId);
            playlistRepository.save(playlist);
        });
        return "redirect:/playlists";
    }
}
