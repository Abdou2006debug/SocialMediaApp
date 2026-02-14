package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;
    private final StorageUtil storageUtil;
    private final TaskScheduler taskScheduler;


    // profile avatar uploading is done directly via the server
    public String uploadFile(MultipartFile file,String userId) throws IOException {

        String bucket=storageEnv.getMedia();

        uploadRequest request=storageUtil.toUploadRequest(file);

        storageUtil.validateRequest(request);

        String filepath =storageUtil.generateFilePath(request,userId);

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        return filepath;
    }

    public void deleteFile(String filepath){
        String bucket=storageEnv.getMedia();
        webClient.delete().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).retrieve().toBodilessEntity().block();
    }

    public boolean checkFileExist(String filepath){
        String bucket=storageEnv.getMedia();
        try{
            webClient.get().
                    uri("/storage/v1/object/info/public/{bucket}/{filename}", bucket, filepath).
                    retrieve().toBodilessEntity().block();
            return true;
        }catch (WebClientResponseException e){
            return false;
        }catch (Exception e){
            log.error("exception while checking file exist "+e.getMessage());
            return false;
        }

    }

    // used to generate a temporary signed url that the client can use to upload files
    public upload generateSignedUrl(uploadRequest request, String userId) {
        String bucket=storageEnv.getMedia();
        storageUtil.validateRequest(request);
        String filepath=storageUtil.generateFilePath(request,userId);
        SignRequest signRequest=new SignRequest(60);
        String signeduri= storageEnv.getUrl()+webClient.post().uri("/storage/v1/object/upload/sign/{bucket}/{filename}",bucket,filepath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signRequest).retrieve().bodyToMono(signResponse.class).map(signResponse::getUrl).block();
        return new upload(filepath,signeduri);
    }

    public void scheduleCleanSupabase(String filepath,long duration){
        Instant limit=Instant.now().plus(duration, ChronoUnit.MINUTES);
        taskScheduler.schedule(()->deleteFile(filepath), limit);
    }

    public void cancelScheduledClean(){

    }


}
