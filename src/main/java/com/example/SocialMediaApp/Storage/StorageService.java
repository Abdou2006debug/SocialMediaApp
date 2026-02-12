package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;
    private final StorageUtil storageUtil;


    // profile avatar uploading is done directly via the server
    public String uploadFile(MultipartFile file,String userId) throws IOException {

        String bucket=storageEnv.getMedia();

        uploadRequest request=storageUtil.toUploadRequest(file);

        storageUtil.validateRequest(request);

        String fileName =storageUtil.generateFilePath(request,userId);

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", bucket, fileName).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return String.format("%s/storage/v1/object/public/%s/%s",
                storageEnv.getUrl(),bucket, encodedFileName);
    }

    public void deleteFile(String filepath){
        String bucket=storageEnv.getMedia();
        webClient.delete().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).retrieve().toBodilessEntity().block();
    }

    // used to generate a temporary signed url that the client can use to upload files
    public String generateSignedUrl(uploadRequest request,String userId) {
        String bucket=storageEnv.getMedia();
        storageUtil.validateRequest(request);
        String filepath=storageUtil.generateFilePath(request,userId);
        SignRequest signRequest=new SignRequest(60);
        return storageEnv.getUrl()+webClient.post().uri("/storage/v1/object/upload/sign/{bucket}/{filename}",bucket,filepath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signRequest).retrieve().bodyToMono(signResponse.class).map(signResponse::getUrl).block();
    }


}
