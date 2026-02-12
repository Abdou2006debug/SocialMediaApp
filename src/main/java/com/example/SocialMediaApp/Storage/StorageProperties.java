package com.example.SocialMediaApp.Storage;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix ="supabase" )
@Getter
public class StorageProperties {
    private String url;
    private String apiKey;
    private String media;
}
