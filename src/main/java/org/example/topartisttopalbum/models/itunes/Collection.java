package org.example.topartisttopalbum.models.itunes;

// Record for Itunes Collection (Album)
public record Collection(
        String wrapperType,
        String collectionType,
        Integer artistId,
        Integer collectionId,
        Integer amgArtistId,
        String artistName,
        String collectionName,
        String collectionCensoredName,
        String artistViewUrl,
        String collectionViewUrl,
        String artworkUrl60,
        String artworkUrl100,
        Double collectionPrice,
        String collectionExplicitness,
        Integer trackCount,
        String copyright,
        String country,
        String currency,
        String releaseDate,
        String primaryGenreName
) implements Result {
}
