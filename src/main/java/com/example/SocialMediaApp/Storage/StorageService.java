package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import lombok.RequiredArgsConstructor;
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
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;
    private final StorageBuckets storageBuckets;

    // profile avatar uploading is done directly via the server
    public String uploadFile(MultipartFile file, String oldAvatarUri,String userId) throws IOException {

        if(oldAvatarUri!=null){
            webClient.delete().uri(oldAvatarUri).retrieve().toBodilessEntity().block();
        }

        uploadRequest request=toUploadRequest(file);

        validateRequest(request);

        String fileName =generateFileName(request,userId);

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", storageBuckets.getProfile_bucket(), fileName).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return String.format("%s/storage/v1/object/public/%s/%s",
                storageEnv.getUrl(), storageBuckets.getProfile_bucket(), encodedFileName);
    }


    public String generateSignedUrl(uploadRequest request,String userId) {
        validateRequest(request);
        String fileName=generateFileName(request,userId);

        return "";
    }

    private String generateFileName(uploadRequest request,String userId ){
        String uuid= UUID.randomUUID().toString();
        return String.format("%s/%s/%s", request.getUploadType().toLowerCase(), userId, uuid);
    }

    private void validateRequest(uploadRequest request){

    }

    private uploadRequest toUploadRequest(MultipartFile file){
        uploadRequest request = new uploadRequest();
        request.setFileName(file.getName());
        request.setFileType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType("Profile");
        return request;
    }
}
