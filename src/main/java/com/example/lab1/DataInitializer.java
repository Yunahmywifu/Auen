package com.example.lab1;

import com.example.lab1.model.Artist;
import com.example.lab1.model.Playlist;
import com.example.lab1.model.Song;
import com.example.lab1.repository.ArtistRepository;
import com.example.lab1.repository.PlaylistRepository;
import com.example.lab1.repository.SongRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;

    public DataInitializer(ArtistRepository artistRepository,
                           SongRepository songRepository,
                           PlaylistRepository playlistRepository) {
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
    }

    @Override
    public void run(String... args) {
        if (artistRepository.count() > 0) return;


        Artist artist1 = artistRepository.save(new Artist("The Beatles", "UK", "Rock", 1960));
        Artist artist2 = artistRepository.save(new Artist("Michael Jackson", "USA", "Pop", 1964));
        Artist artist3 = artistRepository.save(new Artist("Daft Punk", "France", "Electronic", 1993));


        Song song1 = songRepository.save(new Song("Hey Jude", "Past Masters", 431, "Rock", artist1));
        Song song2 = songRepository.save(new Song("Let It Be", "Let It Be", 243, "Rock", artist1));
        Song song3 = songRepository.save(new Song("Billie Jean", "Thriller", 294, "Pop", artist2));
        Song song4 = songRepository.save(new Song("Thriller", "Thriller", 358, "Pop", artist2));
        Song song5 = songRepository.save(new Song("Get Lucky", "Random Access Memories", 369, "Electronic", artist3));
        Song song6 = songRepository.save(new Song("One More Time", "Discovery", 320, "Electronic", artist3));

        Playlist playlist1 = new Playlist("Classic Rock Hits", "Best rock songs of all time", "Admin");
        playlist1.addSong(song1);
        playlist1.addSong(song2);
        playlistRepository.save(playlist1);

        Playlist playlist2 = new Playlist("Pop Legends", "Iconic pop tracks", "Admin");
        playlist2.addSong(song3);
        playlist2.addSong(song4);
        playlistRepository.save(playlist2);

        Playlist playlist3 = new Playlist("Electronic Vibes", "Electronic music collection", "Admin");
        playlist3.addSong(song5);
        playlist3.addSong(song6);
        playlistRepository.save(playlist3);

        System.out.println("✅ Sample data loaded: 3 artists, 6 songs, 3 playlists");
    }
}

