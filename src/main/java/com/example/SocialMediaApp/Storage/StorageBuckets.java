package com.example.SocialMediaApp.Storage;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "")
@Component
@Getter
public class StorageBuckets {
    private String profile_bucket;
    private String content_bucket;
}
