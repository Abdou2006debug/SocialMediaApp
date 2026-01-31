package com.example.whatsappclone.Storage;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix ="" )
@Getter
public class StorageProperties {
    private String url;
    private String apiKey;
}
