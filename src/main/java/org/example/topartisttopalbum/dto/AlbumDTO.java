package org.example.topartisttopalbum.dto;

public record AlbumDTO(String artistName,
                       String collectionName,
                       String collectionCensoredName,
                       Integer trackCount
) {
}
