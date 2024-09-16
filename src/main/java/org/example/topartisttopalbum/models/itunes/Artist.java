package org.example.topartisttopalbum.models.itunes;

// Record for Itunes Artist
public record Artist(
        String wrapperType,
        String artistType,
        String artistName,
        String artistLinkUrl,
        Integer artistId,
        Integer amgArtistId,
        String primaryGenreName,
        Integer primaryGenreId
) implements Result {
}
