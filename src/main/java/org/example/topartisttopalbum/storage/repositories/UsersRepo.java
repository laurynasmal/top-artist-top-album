package org.example.topartisttopalbum.storage.repositories;

import org.example.topartisttopalbum.storage.models.ArtistEnt;
import org.example.topartisttopalbum.storage.models.Users;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UsersRepo extends ReactiveCrudRepository<Users, Long> {
    @Query("select * from users where user_id = :userId")
    Mono<Users> findByUserId(@Param("userId")String userId);

    @Modifying
    @Query("update users set artist_id = :artistIdInDb where user_id = :userId")
    Mono<Void> updateUsersByArtist(@Param("artistIdInDb") Long artistIdInDb, @Param("userId") String userId);
}
