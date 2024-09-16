package org.example.topartisttopalbum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.topartisttopalbum.models.itunes.ItunesResp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SerializationTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testItunesSearchRespSerialization() throws IOException {
        var r = getClass().getResourceAsStream("/itunesSearchResp.txt");
        var itunesResp = objectMapper.readValue(readResourceIs(r), ItunesResp.class);
        Assertions.assertNotNull(itunesResp, "Value should not be null");
        Assertions.assertEquals(60, itunesResp.resultCount());
        Assertions.assertEquals(60, itunesResp.results().size());
    }

    @Test
    public void testItunesTopArtistRespSerialization() throws IOException {
        var r = getClass().getResourceAsStream("/itunesTop5ArtistAlbumsResp.txt");
        var itunesResp = objectMapper.readValue(readResourceIs(r), ItunesResp.class);
        Assertions.assertNotNull(itunesResp, "Value should not be null");
        Assertions.assertEquals(6, itunesResp.resultCount());
        Assertions.assertEquals(6, itunesResp.results().size());
    }

    private String readResourceIs(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}
