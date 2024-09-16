package org.example.topartisttopalbum.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.topartisttopalbum.dto.ArtistDTO;
import org.example.topartisttopalbum.dto.ArtistSearchResDTO;
import org.example.topartisttopalbum.dto.Top5AlbumResDTO;
import org.example.topartisttopalbum.service.UserDataService;
import org.example.topartisttopalbum.service.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LogManager.getLogger();

    private final UserDataService userDataService;

    @PostMapping("/find/artist")
    public Mono<ArtistSearchResDTO> searchArtist(@RequestParam String userId, @RequestParam String artistNameKeyword) {
        return userDataService.searchArtistAsync(userId, artistNameKeyword);
    }

    @PostMapping("/save/artist")
    public Mono<ResponseEntity<Object>> saveFavoriteArtist(@RequestParam String userId, @RequestBody ArtistDTO artist) {
        return userDataService.saveFavoriteArtist(userId, artist)
                .then(Mono.just(ResponseEntity.ok().build())) // Return HTTP 200 OK if successful
                .onErrorResume(this::handleException);
    }

    @PostMapping("/receive/top5albums")
    public Mono<ResponseEntity<Top5AlbumResDTO>> getTop5Albums(@RequestParam String userId) {
        return userDataService.getTop5Albums(userId)
                .map(res -> ResponseEntity.ok().body(res))
                .onErrorResume(this::handleException);
    }


    private <T> Mono<ResponseEntity<T>> handleException(Throwable e) {
        if (e instanceof UserNotFoundException) {
            log.warn("User not found");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        } else if (e instanceof JsonProcessingException) {
            log.error("Currently unable to parse itunes response", e);
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
        } else {
            log.error("Internal server error", e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
}
