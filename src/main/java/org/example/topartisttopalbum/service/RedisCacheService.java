package org.example.topartisttopalbum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final String ITUNES_API_CALLS_KEY = "itunes_api_calls";
    private static final String ITUNES_API_CALLS_TIMESTAMP_KEY = "itunes_api_calls_timestamp";
    private static final String USER_LAST_SEARCH_PREFIX = "user_last_search:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<Integer> initializeItunesApiCalls(int itunesApiMaxCalls) {
        return reactiveRedisTemplate.opsForValue()
                .set(ITUNES_API_CALLS_KEY, String.valueOf(itunesApiMaxCalls))
                .thenReturn(itunesApiMaxCalls);
    }

    public Mono<Void> decrementItunesApiCalls() {
        return reactiveRedisTemplate.opsForValue()
                .decrement(ITUNES_API_CALLS_KEY)
                .then();
    }

    public Mono<Integer> getItunesApiCalls() {
        return reactiveRedisTemplate.opsForValue()
                .get(ITUNES_API_CALLS_KEY)
                .flatMap(value -> {
                    try {
                        return Mono.just(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        return Mono.error(new IllegalStateException("Invalid API call count format", e));
                    }
                });
    }

    public Mono<Boolean> updateItunesApiCallsTimestamp() {
        return reactiveRedisTemplate.opsForValue()
                .set(ITUNES_API_CALLS_TIMESTAMP_KEY, Instant.now().toString());
    }

    public Mono<String> getItunesApiCallsUpdateTimestamp() {
        return reactiveRedisTemplate.opsForValue()
                .get(ITUNES_API_CALLS_TIMESTAMP_KEY);
    }

    public Mono<Void> storeLastUserArtistSearch(String userId, String searchRes) {
        return reactiveRedisTemplate.opsForValue()
                .set(USER_LAST_SEARCH_PREFIX + userId, searchRes)
                .then();
    }

    public Mono<String> getLastUserArtistSearch(String userId) {
        return reactiveRedisTemplate.opsForValue()
                .get(USER_LAST_SEARCH_PREFIX + userId);
    }

}