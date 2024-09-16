package org.example.topartisttopalbum.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.example.topartisttopalbum.models.itunes.Artist;
import org.example.topartisttopalbum.models.itunes.Collection;
import org.example.topartisttopalbum.models.itunes.ItunesResp;
import org.example.topartisttopalbum.models.itunes.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItunesResultDeserializer extends StdDeserializer<ItunesResp> {

    public ItunesResultDeserializer() {
        super(ItunesResp.class);
    }


    @Override
    public ItunesResp deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        int resultCount = node.get("resultCount").asInt();
        List<Result> results = parseResults(node.path("results"));
        return new ItunesResp(resultCount, results);
    }

    private List<Result> parseResults(JsonNode resultsNode) {
        List<Result> results = new ArrayList<>();
        if (resultsNode.isArray()) {
            for (JsonNode resultNode : resultsNode) {
                results.add(parseResult(resultNode));
            }
        }
        return results;
    }

    private Result parseResult(JsonNode node) {
        String wrapperType = node.get("wrapperType").asText();
        return switch (wrapperType) {
            case "artist" -> createArtistFromNode(node, wrapperType);
            case "collection" -> createAlbumCollectionFromNode(node, wrapperType);
            default -> throw new IllegalArgumentException("Unknown wrapper type: " + wrapperType);
        };
    }


    private Artist createArtistFromNode(JsonNode node, String wrapperType) {
        return new Artist(
                wrapperType,
                node.path("artistType").asText(),
                node.path("artistName").asText(),
                node.path("artistLinkUrl").asText(),
                node.path("artistId").asInt(),
                node.path("amgArtistId").asInt(),
                node.path("primaryGenreName").asText(),
                node.path("primaryGenreId").asInt()
        );
    }

    private Collection createAlbumCollectionFromNode(JsonNode node, String wrapperType) {
        return new Collection(
                wrapperType,
                node.path("collectionType").asText(),
                node.path("artistId").asInt(),
                node.path("collectionId").asInt(),
                node.path("amgArtistId").asInt(),
                node.path("artistName").asText(),
                node.path("collectionName").asText(),
                node.path("collectionCensoredName").asText(),
                node.path("artistViewUrl").asText(),
                node.path("collectionViewUrl").asText(),
                node.path("artworkUrl60").asText(),
                node.path("artworkUrl100").asText(),
                node.path("collectionPrice").asDouble(),
                node.path("collectionExplicitness").asText(),
                node.path("trackCount").asInt(),
                node.path("copyright").asText(),
                node.path("country").asText(),
                node.path("currency").asText(),
                node.path("releaseDate").asText(),
                node.path("primaryGenreName").asText()
        );
    }


    private String getTextOrDefault(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText("") : "";
    }

    private Integer getIntOrDefault(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asInt(0) : 0;
    }

    private double getDoubleOrDefault(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asDouble(0.0) : 0.0;
    }
}
