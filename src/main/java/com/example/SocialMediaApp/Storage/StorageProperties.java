package com.example.SocialMediaApp.Storage;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix ="supabase" )
@Component
@Getter
public class StorageProperties {
    private String url;
    private String apiKey;
    private String endpoint;
    private String media;
}
