package com.example.lab1.controller;

import com.example.lab1.model.Artist;
import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import com.example.lab1.repository.ArtistRepository;
import com.example.lab1.repository.PlaylistRepository;
import com.example.lab1.repository.SongRepository;
import com.example.lab1.repository.UserRepository;
import com.example.lab1.service.SpotifyService;
import com.example.lab1.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final SpotifyService spotifyService;

    public AuthController(UserRepository userRepository,
                          UserService userService,
                          ArtistRepository artistRepository,
                          SongRepository songRepository,
                          PlaylistRepository playlistRepository,
                          SpotifyService spotifyService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.spotifyService = spotifyService;
    }

    private void addCurrentUser(Model model, UserDetails userDetails) {
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername())
                .ifPresent(u -> model.addAttribute("currentUser", u));
        }
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam(required = false) String email,
                                Model model) {
        try {
            userService.register(username, password, email);
            return "redirect:/login?registered";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String rootRedirect() { return "redirect:/index"; }

    @GetMapping("/index")
    public String indexPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("songCount",     songRepository.count());
        model.addAttribute("artistCount",   artistRepository.count());
        model.addAttribute("playlistCount", playlistRepository.count());
        model.addAttribute("artists",       artistRepository.findAll());
        model.addAttribute("songs",         songRepository.findAll());

        try {
            model.addAttribute("featuredPlaylists",
                    playlistRepository.findAll().stream().limit(8).toList());
        } catch (Exception e) {
            model.addAttribute("featuredPlaylists", java.util.Collections.emptyList());
        }

        try {
            model.addAttribute("topArtists", spotifyService.getTopArtists());
        } catch (Exception e) {
            model.addAttribute("topArtists", java.util.Collections.emptyList());
        }

        try {
            model.addAttribute("top20Songs", spotifyService.getTop20Charts());
        } catch (Exception e) {
            model.addAttribute("top20Songs", java.util.Collections.emptyList());
        }

        addCurrentUser(model, userDetails);
        return "index";
    }

    @GetMapping("/artists")
    public String artistsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("artists", artistRepository.findAll());
        addCurrentUser(model, userDetails);
        return "artists";
    }

    @GetMapping("/songs")
    public String songsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("songs", songRepository.findAll());
        model.addAttribute("artists", artistRepository.findAll());
        addCurrentUser(model, userDetails);
        return "songs";
    }

    @GetMapping("/playlists")
    public String playlistsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("playlists", playlistRepository.findAll());
        model.addAttribute("availableSongs", songRepository.findAll());
        addCurrentUser(model, userDetails);
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
                           @RequestParam(required = false) String album,
                           @RequestParam(required = false) String genre,
                           @RequestParam(required = false) Integer duration,
                           @RequestParam(required = false) Long artistId,
                           @RequestParam(required = false) String artistName,
                           @RequestParam(required = false) String artistSpotifyId) {
        Song song = new Song();
        song.setTitle(title);
        song.setAlbum(album != null ? album : "");
        song.setGenre(genre != null ? genre : "");
        song.setDuration(duration != null ? duration : 0);

        if (artistId != null) {
            artistRepository.findById(artistId).ifPresent(song::setArtist);
        } else if (artistName != null && !artistName.isBlank()) {
            String trimmedName = artistName.trim();

            Artist artist = artistRepository.findAll()
                    .stream()
                    .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(trimmedName))
                    .findFirst()
                    .orElse(null);

            if (artist == null) {
                artist = new Artist();
                artist.setName(trimmedName);
                artist.setCountry("");
                artist.setGenre(genre != null ? genre : "");
                artist.setYearFounded(0);

                // Try to enrich a new artist with Spotify data.
                try {
                    se.michaelthelin.spotify.model_objects.specification.Artist[] spotifyArtists =
                            spotifyService.searchArtists(trimmedName);

                    if (spotifyArtists != null && spotifyArtists.length > 0) {
                        se.michaelthelin.spotify.model_objects.specification.Artist sp = spotifyArtists[0];

                        if (artistSpotifyId != null && !artistSpotifyId.isBlank()) {
                            se.michaelthelin.spotify.model_objects.specification.Artist matched = Arrays.stream(spotifyArtists)
                                    .filter(a -> artistSpotifyId.equals(a.getId()))
                                    .findFirst()
                                    .orElse(null);
                            if (matched != null) {
                                sp = matched;
                            }
                        }

                        if (sp.getGenres() != null && sp.getGenres().length > 0 && sp.getGenres()[0] != null && !sp.getGenres()[0].isBlank()) {
                            String spGenre = sp.getGenres()[0].trim();
                            String mappedGenre = mapSpotifyGenre(spGenre);
                            artist.setGenre(!mappedGenre.isEmpty() ? mappedGenre : spGenre);
                        }

                        // Spotify Artist object has no direct country field.
                        artist.setCountry("");

                        // Try to infer first active year from top search track release date.
                        try {
                            Track[] tracks = spotifyService.searchTracks("artist:\"" + trimmedName + "\"");
                            if (tracks != null && tracks.length > 0
                                    && tracks[0].getAlbum() != null
                                    && tracks[0].getAlbum().getReleaseDate() != null
                                    && tracks[0].getAlbum().getReleaseDate().length() >= 4) {
                                int year = Integer.parseInt(tracks[0].getAlbum().getReleaseDate().substring(0, 4));
                                artist.setYearFounded(year);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Spotify artist fetch failed: " + e.getMessage());
                }

                artist = artistRepository.save(artist);
            }
            song.setArtist(artist);
        }

        songRepository.save(song);
        return "redirect:/songs";
    }

    private String mapSpotifyGenre(String spotifyGenre) {
        if (spotifyGenre == null) return "";
        String gl = spotifyGenre.toLowerCase();
        if (gl.contains("k-pop") || gl.contains("korean")) return "K-Pop";
        if (gl.contains("j-pop") || gl.contains("japanese")) return "J-Pop";
        if (gl.contains("pop")) return "Pop";
        if (gl.contains("hip hop") || gl.contains("hip-hop") || gl.contains("rap")) return "Hip-Hop";
        if (gl.contains("trap")) return "Trap";
        if (gl.contains("drill")) return "Drill";
        if (gl.contains("r&b") || gl.contains("rnb") || gl.contains("rhythm")) return "R&B";
        if (gl.contains("soul")) return "Soul";
        if (gl.contains("funk")) return "Funk";
        if (gl.contains("house")) return "House";
        if (gl.contains("techno")) return "Techno";
        if (gl.contains("electronic") || gl.contains("edm") || gl.contains("electro")) return "Electronic";
        if (gl.contains("dance") || gl.contains("disco")) return "Dance";
        if (gl.contains("ambient")) return "Ambient";
        if (gl.contains("lo-fi") || gl.contains("lofi")) return "Lo-Fi";
        if (gl.contains("metal")) return "Metal";
        if (gl.contains("punk")) return "Punk";
        if (gl.contains("alternative") || gl.contains("alt rock")) return "Alternative";
        if (gl.contains("indie")) return "Indie";
        if (gl.contains("rock")) return "Rock";
        if (gl.contains("jazz")) return "Jazz";
        if (gl.contains("blues")) return "Blues";
        if (gl.contains("classical") || gl.contains("orchestra")) return "Classical";
        if (gl.contains("opera")) return "Opera";
        if (gl.contains("folk") || gl.contains("acoustic")) return "Folk";
        if (gl.contains("country") || gl.contains("bluegrass")) return "Country";
        if (gl.contains("latin") || gl.contains("reggaeton") || gl.contains("salsa")) return "Latin";
        if (gl.contains("reggae") || gl.contains("dancehall")) return "Reggae";
        if (gl.contains("afro")) return "Afrobeats";
        if (gl.contains("gospel") || gl.contains("christian")) return "Gospel";
        if (gl.contains("soundtrack") || gl.contains("score")) return "Soundtrack";
        return "";
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
