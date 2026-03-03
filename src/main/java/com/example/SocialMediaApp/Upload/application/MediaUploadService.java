package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.*;
import com.example.SocialMediaApp.Storage.StorageService;

import com.example.SocialMediaApp.Upload.api.dto.*;
import com.example.SocialMediaApp.Upload.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class MediaUploadService {

    private final StorageService storageService;
    private final RedisTemplate<String,Object> objectRedisTemplate;
    private final RedisTemplate<String,String> redisTemplate;
    private final UploadStateService uploadStateService;
    private final UploadUtil uploadUtil;
    private final UploadValidationService uploadValidationService;
    private final WebhookVerification webhookVerification;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 30;


    // used to upload files directly though the server
    public String Upload(MultipartFile file, String userId) throws IOException {
        PostUploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFile(uploadRequest);
        UploadInitiation uploadInitiation= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=uploadInitiation.getFilepath();
        // for this method since the uploading is done directly via the server
        // don't need to make the file start with temporary to not get deleted later by the cron job
        storageService.uploadFile(file,filepath.replace("temporary","permanent"));
        return filepath;
    }

    public UploadSession requestUpload(String userId, BaseUploadRequest request){
         uploadValidationService.validateFile(request);
         UploadInitiation uploadInitiation=uploadUtil.generateUploadResponse(userId,request);
         String filepath=uploadInitiation.getFilepath();
         String uploadRequestId=uploadInitiation.getUploadRequestId();
         String signedUrl=storageService.generateSignedUrl(filepath);
         // creating an upload session containing the user id for authorization later + the request id and the upload type
        return UploadSession.builder()
                .userId(userId).signedUrl(signedUrl).
                uploadRequestId(uploadRequestId)
                .filePath(filepath).build();
    }

    public void confirmUpload(String signature, SupabaseWebhookPayload webhookPayload){

        String filePath=null;
        webhookVerification.verifySignature(signature);

        try{

             filePath=webhookPayload.getRecord().getName();

            UploadSession uploadSession=uploadStateService.validateUploadSession(null,filePath, UploadPhase.REQUESTED);

            webhookVerification.verifyFileUploaded(uploadSession,webhookPayload.getRecord());

            if(uploadSession.getUploadType()==UploadType.POST){
                objectRedisTemplate.opsForValue().set(uploadSession.getUploadRequestId(),uploadSession,UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);
            }else{

                    String batchId=uploadSession.getBatchId();
                    boolean batchExists=batchId!=null&&redisTemplate.hasKey(batchId);
                    if(!batchExists) throw new ActionNotAllowedException("");
                redisTemplate.opsForHash().put(
                        batchId,
                        "file:" + filePath,
                        uploadSession.getMediaType()
                );
            }



        }catch (ActionNotAllowedException | UnsupportedMediaTypeException | FileTooLargeException e){
            storageService.deleteFile(filePath);
        }


    }

    public void deleteUpload(String userId,String uploadRequestId){

        // upload state must confirmed to delete the file
        UploadSession uploadSession =uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);
        storageService.deleteFile(uploadSession.getFilePath());
        redisTemplate.delete(uploadSession.getUploadRequestId());
    }


}
