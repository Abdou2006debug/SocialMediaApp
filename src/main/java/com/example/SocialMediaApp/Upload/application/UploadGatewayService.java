package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.UploadFailedException;
import com.example.SocialMediaApp.Storage.StorageService;

import com.example.SocialMediaApp.Upload.api.dto.uploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.uploadResponse;
import com.example.SocialMediaApp.Upload.domain.uploadPhase;
import com.example.SocialMediaApp.Upload.domain.uploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UploadGatewayService {

    private final StorageService storageService;
    private final RedisTemplate<String,String> redisTemplate;
    private final UploadStateService uploadStateService;
    private final UploadUtil uploadUtil;
    private final UploadValidationService uploadValidationService;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 10;


    public uploadResponse requestUpload(String userId, uploadRequest uploadRequest){
        uploadValidationService.validateFileUpload(uploadRequest);
         Map<String,String> uploadMap=uploadUtil.generateUploadResponse(userId,uploadRequest);
         String filepath=uploadMap.get("filepath");
         String requestId=uploadMap.get("requestId");
         String signedUrl=storageService.generateSignedUrl(filepath);

        redisTemplate.opsForValue().set(String.format("requested:%s",requestId),filepath,UPLOAD_WAIT_DURATION_MINUTES, TimeUnit.MINUTES);
        return new uploadResponse(signedUrl,requestId);
    }
    public void confirmUpload(String userId,String uploadRequestId){

            String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId, uploadPhase.REQUESTED);

            boolean fileExist=storageService.checkFileExist(filepath);

            if(!fileExist) throw new UploadFailedException("Upload failed try again");

            redisTemplate.opsForValue().set(String.format("confirmed:%s", uploadRequestId),filepath,UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);

            storageService.scheduleCleanSupabase(filepath,UPLOAD_CONFIRM_DURATION_MINUTES);

    }

    public void deleteUpload(String userId,String uploadRequestId){

        String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId,uploadPhase.CONFIRMED);

        redisTemplate.delete(filepath);
        storageService.cancelScheduledClean(filepath);
    }

    public List<Media> finalizeUploads(String userId, List<String> uploadRequestsIds, uploadType uploadType){

        List<String> filesPaths =new ArrayList<>();

        for(String uploadRequestId : uploadRequestsIds){
            String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId, uploadPhase.CONFIRMED);
            filesPaths.add(filepath);
        }

        // if we get here that means all files are valid and we untrack them
        filesPaths.forEach(filepath->{
            redisTemplate.delete(filepath);
            storageService.cancelScheduledClean(filepath);
        });


        return uploadUtil.convertToMedia(filesPaths);

            }


    public String Upload(MultipartFile file, String userId) throws IOException {
        uploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFileUpload(uploadRequest);
        Map<String,String> map= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=map.get("filepath");
        storageService.uploadFile(file,filepath);
        return filepath;
    }

}
