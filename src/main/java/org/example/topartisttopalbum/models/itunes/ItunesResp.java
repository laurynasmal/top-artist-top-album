package org.example.topartisttopalbum.models.itunes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.example.topartisttopalbum.serialization.ItunesResultDeserializer;

import java.util.List;

// Record for the Itunes resp
@JsonDeserialize(using = ItunesResultDeserializer.class)
public record ItunesResp(int resultCount, List<Result> results) {}






