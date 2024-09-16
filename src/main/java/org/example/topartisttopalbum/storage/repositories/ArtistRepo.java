package org.example.topartisttopalbum.storage.repositories;

import org.example.topartisttopalbum.storage.models.ArtistEnt;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ArtistRepo extends ReactiveCrudRepository<ArtistEnt, Long> {
    Mono<ArtistEnt> findByArtistId(Integer artistId);

    @Query("select * from artist_ent where id in (select u.artist_id from users u where u.user_id = :userId)")
    Mono<ArtistEnt> findByUserId(@Param("userId") String userId);

    @Modifying
    @Query("update artist_ent set last_update_time = :lastUpdateTime, itunes_top5_albums_resp = :responseFromItunes where amg_artist_id = :amgArtistId")
    Mono<Void> updateArtistEntByItunesRespAndLastCheckDate(@Param("amgArtistId") Integer amgArtistId, @Param("lastUpdateTime") Instant lastUpdateTime, @Param("responseFromItunes") String responseFromItunes);

    @Query("select * from artist_ent a order by a.last_update_time desc limit :limit")
    Flux<ArtistEnt> findArtistLatelyUpdatedWithLimit(@Param("limit") Integer limit);
}
