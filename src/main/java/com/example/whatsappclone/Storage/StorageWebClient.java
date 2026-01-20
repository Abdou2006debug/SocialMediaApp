package com.example.whatsappclone.Storage;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class StorageWebClient {

    //private final StorageConfigurationVar storageConfigurationVar;
    private final WebClient.Builder webClientBuilder;
    @Bean
    public WebClient webClient(){
        return webClientBuilder.baseUrl(null).defaultHeaders(headers -> {
            headers.set("Authorization", "Bearer " +null);
            headers.set("apikey", null);
            headers.set("x-upsert", "true");
        }).build();
    }
}
