package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.UploadFailedException;
import com.example.SocialMediaApp.Shared.Exceptions.UploadTypeMismatch;
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


    // used to upload files directly though the server
    public String Upload(MultipartFile file, String userId) throws IOException {
        uploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFileUpload(uploadRequest);
        Map<String,String> map= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=map.get("filepath");
        storageService.uploadFile(file,filepath);
        return filepath;
    }

    public uploadResponse requestUpload(String userId, uploadRequest uploadRequest){
        uploadValidationService.validateFileUpload(uploadRequest);
         Map<String,String> uploadMap=uploadUtil.generateUploadResponse(userId,uploadRequest);
         String filepath=uploadMap.get("filepath");
         String uploadRequestId=uploadMap.get("uploadRequestId");
         String signedUrl=storageService.generateSignedUrl(filepath);

        redisTemplate.opsForValue().set(String.format("requested:%s",uploadRequestId),filepath,UPLOAD_WAIT_DURATION_MINUTES, TimeUnit.MINUTES);
        return new uploadResponse(signedUrl,uploadRequestId);
    }

    public int confirmUpload(String userId,String uploadRequestId){

            String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId, uploadPhase.REQUESTED);

            boolean fileExist=storageService.checkFileExist(filepath);

            if(!fileExist) throw new UploadFailedException("Upload failed try again");

            redisTemplate.opsForValue().set(String.format("confirmed:%s", uploadRequestId),filepath,UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);

            redisTemplate.delete(String.format("requested:%s",uploadRequestId));

            storageService.scheduleCleanSupabase(filepath,UPLOAD_CONFIRM_DURATION_MINUTES);

            return UPLOAD_CONFIRM_DURATION_MINUTES;
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
            uploadValidationService.confirmUploadType(filepath, com.example.SocialMediaApp.Upload.domain.uploadType.POST);
            filesPaths.add(filepath);
        }

        // if we get here that means all files are valid and we untrack them
        filesPaths.forEach(filepath->{
            redisTemplate.delete(String.format("confirmed:%s",filepath.split("/")[3]));
            storageService.cancelScheduledClean(filepath);
        });


        return uploadUtil.convertToMedia(filesPaths);

            }

}
