package com.example.lab1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "artist")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;
    private String genre;
    private int yearFounded;

    public Artist() {}

    public Artist(String name, String country, String genre, int yearFounded) {
        this.name = name;
        this.country = country;
        this.genre = genre;
        this.yearFounded = yearFounded;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getYearFounded() { return yearFounded; }
    public void setYearFounded(int yearFounded) { this.yearFounded = yearFounded; }
}
