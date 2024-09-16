package org.example.topartisttopalbum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TopArtistTopAlbumApplication {
    public static void main(String[] args) {
        SpringApplication.run(TopArtistTopAlbumApplication.class, args);
    }
}
