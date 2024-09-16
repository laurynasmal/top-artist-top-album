package org.example.topartisttopalbum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.topartisttopalbum.models.itunes.ItunesResp;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItunesService {
    public static final int MAX_API_CALLS = 100;
    private static final Logger log = LogManager.getLogger();

    private final WebClient itunesClient;

    private final ObjectMapper objectMapper;

    private final RedisCacheService redisCacheService;


    public Mono<ItunesResp> searchArtists(String entity, String artistNameKeyword) {
        return checkIfCallCanBeMade()
                .then(itunesClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search")
                                .queryParam("entity", entity)
                                .queryParam("term", artistNameKeyword)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(this::parseResponse)
                        .doOnError(e -> log.error("Error searching artists", e)));
    }

    public Mono<ItunesResp> getTopAlbums(String amgArtistId, String entity, int maxAlbum) {
        return checkIfCallCanBeMade()
                .then(itunesClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/lookup")
                                .queryParam("amgArtistId", amgArtistId)
                                .queryParam("entity", entity)
                                .queryParam("limit", maxAlbum)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(this::parseResponse)
                        .doOnError(e -> log.error("Error getting top albums", e)))
                ;
    }

    public Mono<Void> checkIfCallCanBeMade() {
        return redisCacheService.getItunesApiCalls()
                .flatMap(currentApiCalls -> {
                            if (currentApiCalls > 0) {
                                return redisCacheService.decrementItunesApiCalls();
                            } else {
                                log.warn("No API calls left");
                                return Mono.error(new RuntimeException("No API calls left"));
                            }
                        }
                )
                .doOnError(e -> log.error("Error performing API call", e));
    }

    private Mono<ItunesResp> parseResponse(String response) {
        try {
            ItunesResp itunesResp = objectMapper.readValue(response, ItunesResp.class);
            return Mono.just(itunesResp);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error parsing response", e));
        }
    }

}
