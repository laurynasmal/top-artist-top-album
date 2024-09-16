package org.example.topartisttopalbum.scheduling;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.topartisttopalbum.service.ItunesService;
import org.example.topartisttopalbum.service.RedisCacheService;
import org.example.topartisttopalbum.service.UserDataService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ItunesApiCallResetTask implements InitializingBean {
    private static final int API_CALL_THRESHOLD = 10;

    private static final Logger log = LogManager.getLogger();

    private final RedisCacheService redisDataService;
    private final UserDataService userDataService;

    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void resetApiCallCounters() {
        log.info("Running reset api call counter cron task");
        resetApiCallLimits().subscribe(
                null,
                e -> log.error("Failed to reset API call limits", e)
        );
    }

    @Scheduled(cron = "0 55 * * * ?")
    public void runTask() {
        log.info("Running top 5 albums refresh task");
        redisDataService.getItunesApiCalls()
                .filter(callsLeft -> callsLeft > API_CALL_THRESHOLD)
                .flatMap(callsLeft -> userDataService.syncLatelyUpdatedAlbums(callsLeft - API_CALL_THRESHOLD))
                .doOnError(e -> log.error("Error occurred while syncing lately updated albums", e))
                .doOnSuccess(v -> log.info("Top 5 albums refresh task completed successfully"))
                .subscribe();
    }


    @Override
    public void afterPropertiesSet() {
        log.info("initializing");
        redisDataService.getItunesApiCalls()
                .switchIfEmpty(resetApiCallLimits())
                .then(redisDataService.getItunesApiCallsUpdateTimestamp()
                        .flatMap(timestamp -> {
                            Instant lastCheckUp = Instant.parse(timestamp);
                            return Duration.between(lastCheckUp, Instant.now()).toHours() >= 1
                                    ? resetApiCallLimits()
                                    : Mono.empty();
                        }))
                .subscribe(
                        null,
                        e -> log.error("Initialization error", e)
                );
    }

    private Mono<Integer> resetApiCallLimits() {
        return Mono.zip(redisDataService.initializeItunesApiCalls(ItunesService.MAX_API_CALLS), redisDataService.updateItunesApiCallsTimestamp())
                .doOnSubscribe(sub -> log.info("API call limits reset"))
                .then(Mono.just(ItunesService.MAX_API_CALLS));
    }

}
