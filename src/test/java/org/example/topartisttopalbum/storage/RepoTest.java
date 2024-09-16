package org.example.topartisttopalbum.storage;

import org.example.topartisttopalbum.storage.models.ArtistEnt;
import org.example.topartisttopalbum.storage.models.Users;
import org.example.topartisttopalbum.storage.repositories.ArtistRepo;
import org.example.topartisttopalbum.storage.repositories.UsersRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataR2dbcTest
public class RepoTest {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private ArtistRepo artistRepo;

    @Autowired
    private R2dbcEntityTemplate template;

    @AfterEach
    void afterEach() {
        usersRepo.deleteAll().block();
        artistRepo.deleteAll().block();
    }


    @Test
    void whenArtistExists_thenFindByArtistIdReturnsArtist() {
        // Given
        ArtistEnt artistEnt = new ArtistEnt(null, "type", "name", "link", 123, 456, "genre", 789, Instant.now(), null);
        artistRepo.save(artistEnt).block();

        // When
        Mono<ArtistEnt> result = artistRepo.findByArtistId(123);

        // Then
        StepVerifier.create(result)
                .assertNext(a -> {
                    assertThat(a).isNotNull();
                    assertThat(a.getArtistId()).isEqualTo(123);
                })
                .verifyComplete();
    }

    @Test
    void testFindByArtistId_whenArtistDoesNotExist_returnsEmpty() {
        // When
        Mono<ArtistEnt> result = artistRepo.findByArtistId(1234);

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenArtistExists_thenUpdateArtistUpdatesArtist() {
        // Given
        ArtistEnt artistEnt = new ArtistEnt(null, "type", "name", "link", 123, 456, "genre", 789, Instant.now(), null);
        artistRepo.save(artistEnt).block();

        // Update
        String updatedRespFromItunes = "updatedResp";
        Instant now = Instant.now();
        Mono<Void> update = artistRepo.updateArtistEntByItunesRespAndLastCheckDate(456, now, updatedRespFromItunes);

        // When
        StepVerifier.create(update)
                .verifyComplete();

        // Then
        Mono<ArtistEnt> result = artistRepo.findByArtistId(123);
        StepVerifier.create(result)
                .assertNext(a -> assertThat(a.getItunesTop5AlbumsResp()).isEqualTo(updatedRespFromItunes))
                .verifyComplete();
    }

    @Test
    void whenUserExists_thenFindByUserIdReturnsUser() {
        // Given
        Users user = new Users(null, "user123", null);
        usersRepo.save(user).block();

        // When
        Mono<Users> result = usersRepo.findByUserId("user123");

        // Then
        StepVerifier.create(result)
                .assertNext(foundUser -> {
                    assertThat(foundUser).isNotNull();
                    assertThat(foundUser.getUserId()).isEqualTo("user123");
                })
                .verifyComplete();
    }

    @Test
    void testFindByUserId_whenUserDoesNotExist_returnsEmpty() {
        // When
        Mono<Users> result = usersRepo.findByUserId("nonexistent");

        // Then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
}
