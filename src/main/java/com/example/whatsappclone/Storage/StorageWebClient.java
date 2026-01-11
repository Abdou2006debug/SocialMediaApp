package com.example.whatsappclone.Storage;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "storage.bucket")
public class StorageWebClient {

    private final WebClient.Builder webClientBuilder;
    private String storageUri;
    private String apiKey;
    @Bean
    public WebClient webClient(){
        return webClientBuilder.baseUrl(storageUri).defaultHeaders(headers -> {
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("apikey", apiKey);
            headers.set("x-upsert", "true");
        }).build();
    }
}
