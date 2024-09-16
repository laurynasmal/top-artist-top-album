package org.example.topartisttopalbum.storage.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("artist_ent")
public class ArtistEnt {
    @Id
    private Long id;
    private String artistType;
    private String artistName;
    private String artistLinkUrl;
    private Integer artistId;
    private Integer amgArtistId;
    private String primaryGenreName;
    private Integer primaryGenreId;
    private Instant lastUpdateTime;
    private String itunesTop5AlbumsResp;
}
