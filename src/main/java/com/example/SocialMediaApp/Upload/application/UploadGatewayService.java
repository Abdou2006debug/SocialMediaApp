package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Exceptions.FileTooLargeException;
import com.example.SocialMediaApp.Shared.Exceptions.UnsupportedMediaTypeException;
import com.example.SocialMediaApp.Storage.StorageService;

import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.UploadResponse;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import com.example.SocialMediaApp.Upload.domain.UploadPhase;
import com.example.SocialMediaApp.Upload.domain.UploadType;
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
    private final WebhookVerification webhookVerification;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 10;


    // used to upload files directly though the server
    public String Upload(MultipartFile file, String userId) throws IOException {
        UploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFile(uploadRequest);
        Map<String,String> map= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=map.get("filepath");
        // for this method since the uploading is done directly via the server dont need to make the file start with temporary to not get deleted later
        filepath=filepath.replace("temporary/","");
        storageService.uploadFile(file,filepath);
        return filepath;
    }

    public UploadResponse requestUpload(String userId, UploadRequest uploadRequest){
         uploadValidationService.validateFile(uploadRequest);
         Map<String,String> uploadMap=uploadUtil.generateUploadResponse(userId,uploadRequest);
         String filepath=uploadMap.get("filepath");
         String uploadRequestId=uploadMap.get("uploadRequestId");
         String signedUrl=storageService.generateSignedUrl(filepath);

        redisTemplate.opsForValue().set(String.format("requested:%s",uploadRequestId),filepath,UPLOAD_WAIT_DURATION_MINUTES, TimeUnit.MINUTES);
        return new UploadResponse(signedUrl,uploadRequestId);
    }

    public void confirmUpload(String signature, SupabaseWebhookPayload webhookPayload){

        String filePath=null;
        String uploadRequestId=null;

        try{

            webhookVerification.verifySignature(signature);

            uploadRequestId=webhookPayload.getRecord().getPathTokens().get(4);

            filePath=uploadStateService.validateUploadSession(null,uploadRequestId, UploadPhase.REQUESTED);

            if(!filePath.equals(webhookPayload.getRecord().getName())) throw new ActionNotAllowedException("Action could not be completed");

            webhookVerification.verifyFileUploaded(filePath,webhookPayload.getRecord());

            redisTemplate.opsForValue().set(String.format("confirmed:%s", uploadRequestId),filePath,UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);

            redisTemplate.delete(String.format("requested:%s",uploadRequestId));

        }catch (ActionNotAllowedException | UnsupportedMediaTypeException | FileTooLargeException e){
            storageService.deleteFile(filePath);
            redisTemplate.delete(String.format("requested:%s",uploadRequestId));
        }


    }

    public void deleteUpload(String userId,String uploadRequestId){

        String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);

        redisTemplate.delete(filepath);

    }

    public List<Media> finalizeUploads(String userId, List<String> uploadRequestsIds, UploadType uploadType){

        List<String> filesPaths =new ArrayList<>();

        for(String uploadRequestId : uploadRequestsIds){
            String filepath=uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);
            uploadValidationService.confirmUploadType(filepath, uploadType);
            filesPaths.add(filepath);
        }

        // if we get here that means all files are valid and we untrack them
        filesPaths.forEach(filepath->{
            redisTemplate.delete(String.format("confirmed:%s",filepath.split("/")[4]));
        });

        storageService.moveFilesToPermanent(filesPaths);

        return uploadUtil.convertToMedia(filesPaths);

            }

}
