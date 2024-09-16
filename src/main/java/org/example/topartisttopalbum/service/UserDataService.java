package org.example.topartisttopalbum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.topartisttopalbum.dto.AlbumDTO;
import org.example.topartisttopalbum.dto.ArtistDTO;
import org.example.topartisttopalbum.dto.ArtistSearchResDTO;
import org.example.topartisttopalbum.dto.Top5AlbumResDTO;
import org.example.topartisttopalbum.models.itunes.Artist;
import org.example.topartisttopalbum.models.itunes.Collection;
import org.example.topartisttopalbum.models.itunes.ItunesResp;
import org.example.topartisttopalbum.service.exceptions.UserNotFoundException;
import org.example.topartisttopalbum.storage.models.ArtistEnt;
import org.example.topartisttopalbum.storage.models.Users;
import org.example.topartisttopalbum.storage.repositories.ArtistRepo;
import org.example.topartisttopalbum.storage.repositories.UsersRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDataService {
    private static final Logger log = LogManager.getLogger();
    private static final String ALL_ARTIST = "allArtist";
    private static final String ALBUM_ENTITY = "album";
    private static final int TOP_ALBUM_LIMIT = 5;

    private final ObjectMapper objectMapper;
    private final RedisCacheService redisCacheService;
    private final ItunesService itunesService;
    private final UsersRepo usersRepo;
    private final ArtistRepo artistRepo;


    public Mono<ArtistSearchResDTO> searchArtistAsync(String userId, String artistNameKeyword) {
        return itunesService.searchArtists(ALL_ARTIST, artistNameKeyword)
                .flatMap(result -> processArtistResultExecution(userId, result))
                .map(this::convertToArtistSearchRes)
                .onErrorResume(e -> {
                    log.error("Error during artist search", e);
                    return Mono.just(new ArtistSearchResDTO(Collections.emptyList()));
                });
    }

    private Mono<ItunesResp> processArtistResultExecution(String userId, ItunesResp result) {
        return usersRepo.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> usersRepo.save(new Users(null, userId, null))))
                .then(redisCacheService.storeLastUserArtistSearch(userId, serializeResult(result)))
                .thenReturn(result);
    }

    public Mono<ArtistEnt> saveFavoriteArtist(String userId, ArtistDTO artistDto) {
        return usersRepo.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(usr -> getArtistFromCache(usr.getUserId(), (int) artistDto.artistId())
                        .flatMap(artist -> handleSaveUserFavoriteArtist(userId, artist))
                        .flatMap(artist -> updateUserFavoriteArtistTopAlbums(artist).then(Mono.just(artist))));
    }

    public Mono<Top5AlbumResDTO> getTop5Albums(String userId) {
        return usersRepo.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(user -> artistRepo.findByUserId(userId))
                .flatMap(this::parseAlbumsFromArtist)
                ;
    }

    private Mono<Top5AlbumResDTO> parseAlbumsFromArtist(ArtistEnt artistEnt) {
        return Mono.fromCallable(() -> {
            try {
                return artistEnt.getItunesTop5AlbumsResp() != null
                        ? objectMapper.readValue(artistEnt.getItunesTop5AlbumsResp(), Top5AlbumResDTO.class)
                        : new Top5AlbumResDTO(Collections.emptyList());
            } catch (JsonProcessingException e) {
                log.error("Error parsing artist's top albums", e);
                return new Top5AlbumResDTO(Collections.emptyList());
            }
        });
    }

    private Top5AlbumResDTO parseAlbumsFromItunesRespSync(ItunesResp itunesResp) {
        List<AlbumDTO> albums = itunesResp.results().stream()
                .filter(Collection.class::isInstance)
                .map(Collection.class::cast)
                .map(collection -> new AlbumDTO(
                        collection.artistName(),
                        collection.collectionName(),
                        collection.collectionCensoredName(),
                        collection.trackCount()))
                .collect(Collectors.toList());
        return new Top5AlbumResDTO(albums);
    }

    private Mono<Void> updateUserFavoriteArtistTopAlbums(ArtistEnt artistEnt) {
        return Mono.just(artistEnt)
                .filter(a -> a.getAmgArtistId() != null && a.getAmgArtistId() > 1 && a.getItunesTop5AlbumsResp() == null)
                .flatMap(a -> itunesService.getTopAlbums(String.valueOf(a.getAmgArtistId()), ALBUM_ENTITY, TOP_ALBUM_LIMIT)
                        .flatMap(itunesResp -> serializeAndUpdateArtistTop5(a, itunesResp))
                        .doOnError(e -> log.error("Error updating artist top albums", e))
                        .then());
    }

    private Mono<Void> serializeAndUpdateArtistTop5(ArtistEnt artistEnt, ItunesResp itunesResp) {
        try {
            String albumsJson = objectMapper.writeValueAsString(parseAlbumsFromItunesRespSync(itunesResp));
            return updateArtistTop5(artistEnt.getAmgArtistId(), albumsJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing albums response to JSON", e);
            return Mono.empty();
        }
    }


    private Mono<Void> updateArtistTop5(Integer amgArtistId, String resp) {
        return artistRepo.updateArtistEntByItunesRespAndLastCheckDate(amgArtistId, Instant.now(), resp);
    }

    public Mono<List<Void>> syncLatelyUpdatedAlbums(int limit) {
        return artistRepo.findArtistLatelyUpdatedWithLimit(limit)
                .flatMap(this::updateUserFavoriteArtistTopAlbums)
                .collectList();
    }

    private ArtistSearchResDTO convertToArtistSearchRes(ItunesResp response) {
        var artistList = response.results().stream()
                .filter(Artist.class::isInstance)
                .map(Artist.class::cast)
                .map(a -> new ArtistDTO(a.artistName(), a.artistId()))
                .collect(Collectors.toList());
        return new ArtistSearchResDTO(artistList);
    }

    private Mono<Artist> getArtistFromCache(String userId, Integer artistId) {
        return redisCacheService.getLastUserArtistSearch(userId)
                .flatMap(json -> {
                    try {
                        ItunesResp itunesResp = objectMapper.readValue(json, ItunesResp.class);
                        return Mono.justOrEmpty(
                                itunesResp.results().stream()
                                        .filter(Artist.class::isInstance)
                                        .map(Artist.class::cast)
                                        .filter(a -> a.artistId().equals(artistId))
                                        .findFirst()
                        );
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing artist from cache", e);
                        return Mono.error(new RuntimeException("Error parsing artist from cache", e));
                    }
                });
    }

    private Mono<ArtistEnt> handleSaveUserFavoriteArtist(String userId, Artist artist) {
        return usersRepo.findByUserId(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
                .flatMap(user -> processArtistUpdate(user, artist));
    }

    private Mono<ArtistEnt> processArtistUpdate(Users user, Artist artist) {
        return artistRepo.findByArtistId(artist.artistId())
                .switchIfEmpty(Mono.defer(() -> saveNewArtist(artist)))
                .flatMap(artistEnt -> usersRepo.updateUsersByArtist(artistEnt.getId(), user.getUserId())
                        .thenReturn(artistEnt));
    }

    private Mono<ArtistEnt> saveNewArtist(Artist artist) {
        ArtistEnt newArtist = new ArtistEnt(
                null, artist.artistType(), artist.artistName(), artist.artistLinkUrl(),
                artist.artistId(), artist.amgArtistId(), artist.primaryGenreName(),
                artist.primaryGenreId(), null, null);
        return artistRepo.save(newArtist);
    }

    private String serializeResult(ItunesResp result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Error serializing artist search result", e);
            throw new RuntimeException("Error serializing artist search result", e);
        }
    }

}