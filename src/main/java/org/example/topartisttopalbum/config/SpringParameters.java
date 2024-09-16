package org.example.topartisttopalbum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SpringParameters {

    @Value("${itunes.api.url:}")
    private String itunesApi;
}
