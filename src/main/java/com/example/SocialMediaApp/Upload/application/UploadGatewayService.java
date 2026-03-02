package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.*;
import com.example.SocialMediaApp.Storage.StorageService;

import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.UploadResponse;
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
public class UploadGatewayService {

    private final StorageService storageService;
    private final RedisTemplate<String,Object> redisTemplate;
    private final UploadStateService uploadStateService;
    private final UploadUtil uploadUtil;
    private final UploadValidationService uploadValidationService;
    private final WebhookVerification webhookVerification;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 30;


    // used to upload files directly though the server
    public String Upload(MultipartFile file, String userId) throws IOException {
        UploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFile(uploadRequest);
        UploadInitiation uploadInitiation= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=uploadInitiation.getFilepath();
        // for this method since the uploading is done directly via the server
        // don't need to make the file start with temporary to not get deleted later by the cron job
        storageService.uploadFile(file,filepath.replace("temporary","permanent"));
        return filepath;
    }

    public UploadResponse requestUpload(String userId, UploadRequest uploadRequest){
         uploadValidationService.validateFile(uploadRequest);
         UploadInitiation uploadInitiation=uploadUtil.generateUploadResponse(userId,uploadRequest);
         String filepath=uploadInitiation.getFilepath();
         String uploadRequestId=uploadInitiation.getUploadRequestId();
         String signedUrl=storageService.generateSignedUrl(filepath);
         // creating an upload session containing the user id for authorization later + the request id and the upload type
        UploadSession uploadSession= UploadSession.builder()
                .userId(userId).uploadRequestId(uploadRequestId)
                        .uploadType(uploadRequest.getUploadType()).filePath(filepath).build();

        redisTemplate.opsForValue().set(filepath,uploadSession,UPLOAD_WAIT_DURATION_MINUTES, TimeUnit.MINUTES);
        return new UploadResponse(signedUrl,uploadRequestId);
    }

    public void confirmUpload(String signature, SupabaseWebhookPayload webhookPayload){

        String filePath=null;
        webhookVerification.verifySignature(signature);

        try{

             filePath=webhookPayload.getRecord().getName();

            UploadSession uploadSession=uploadStateService.validateUploadSession(null,filePath, UploadPhase.REQUESTED);

            webhookVerification.verifyFileUploaded(uploadSession,webhookPayload.getRecord());

            redisTemplate.opsForValue().set(uploadSession.getUploadRequestId(),uploadSession,UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);


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

    public List<Media> finalizePostUploads(String userId, List<String> uploadRequestsIds){

        List<String> filesPaths =new ArrayList<>();
        List<Media> mediaList=new ArrayList<>();
        List<String> failedUploadIds=new ArrayList<>();

        for(String uploadRequestId : uploadRequestsIds){
            try{
            UploadSession uploadSession=uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);
            UploadType actualUploadType=uploadSession.getUploadType();
            if(actualUploadType!=UploadType.POST) throw new UploadTypeMismatch("");
            String filepath=uploadSession.getFilePath();
            filesPaths.add(filepath);
            mediaList.add(new Media(filepath.replace("temporary","permanent"), uploadSession.getMediaType()));
            }catch (ActionNotAllowedException|UploadTypeMismatch e){
                failedUploadIds.add(uploadRequestId);
            }
        }

        if(!failedUploadIds.isEmpty()) throw new UploadFailedException(failedUploadIds);

        storageService.moveFilesToPermanent(filesPaths);

        redisTemplate.delete(uploadRequestsIds);

        return mediaList;

    }

}
