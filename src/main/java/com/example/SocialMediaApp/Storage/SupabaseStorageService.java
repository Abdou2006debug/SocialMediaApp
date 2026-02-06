package com.example.SocialMediaApp.Storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class SupabaseStorageService implements StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;

    @Value("")
    private String profile;
    public String uploadAvatartoStorage(MultipartFile file, String oldAvatarUri) throws IOException {
        if(oldAvatarUri!=null){
            webClient.delete().uri(oldAvatarUri).retrieve().toBodilessEntity();
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", profile, fileName).
                header(HttpHeaders.CONTENT_TYPE, "image/webp").
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return String.format("%s/storage/v1/object/public/%s/%s",
                storageEnv.getUrl(), profile, encodedFileName);

    }
}
