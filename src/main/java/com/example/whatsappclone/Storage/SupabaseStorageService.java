package com.example.whatsappclone.Storage;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "storage.bucket")
public class SupabaseStorageService implements StorageService {

    private final WebClient webClient;
    private String storageUri;
    private String profileAvatars;

    public String uploadAvatartoStorage(MultipartFile file, String oldAvatarUri) throws IOException {
        if(oldAvatarUri!=null){
            webClient.delete().uri(oldAvatarUri).retrieve().toBodilessEntity();
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", profileAvatars, fileName).
                header(HttpHeaders.CONTENT_TYPE, "image/webp").
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return String.format("%s/storage/v1/object/public/%s/%s",
                storageUri, profileAvatars, encodedFileName);

    }
}
