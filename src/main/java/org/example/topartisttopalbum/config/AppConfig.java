package org.example.topartisttopalbum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final SpringParameters springParameters;

    @Bean
    public WebClient itunesClient(){
        return WebClient.builder()
                .baseUrl(springParameters.getItunesApi())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.ALL_VALUE)  // Accept all content types
                .build();
    }
}
