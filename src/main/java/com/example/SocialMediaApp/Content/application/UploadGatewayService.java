package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.uploadAction;
import com.example.SocialMediaApp.Content.api.dto.uploadConfirm;
import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.upload;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UploadGatewayService {

    private final StorageService storageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final RedisTemplate<String,String> redisTemplate;

    public String requestUpload(uploadRequest uploadRequest){
        String currentUserId=authenticatedUserService.getcurrentuser();
        upload upload=storageService.generateSignedUrl(uploadRequest,currentUserId);
        String filepath=upload.getFilepath();
        redisTemplate.opsForValue().set(filepath,"waiting",5, TimeUnit.MINUTES);
        return upload.getSigneduri();
    }

    public void confirmUpload(String filepath){
        if(checkUserOwnership(filepath)){
            String status=redisTemplate.opsForValue().get(filepath);
            if(status==null||!status.equals("waiting")){
                throw new RuntimeException("Upload Session expired or Invalid");
            }
            boolean fileExist=storageService.checkFileExist(filepath);
            if(!fileExist){
                throw new RuntimeException("Upload failed try again");
            }
            redisTemplate.opsForValue().set(filepath,"uploaded",10,TimeUnit.MINUTES);
            storageService.scheduleCleanSupabase(filepath,10);
        }
    }

    public void handleUpload(String filepath, uploadAction uploadAction){
        if(checkUserOwnership(filepath)){
            String status=redisTemplate.opsForValue().get(filepath);
            if(status!=null&&status.equals("uploaded")){
                redisTemplate.delete(filepath);
                storageService.cancelScheduledClean();
                if(uploadAction==com.example.SocialMediaApp.Content.api.dto.uploadAction.DELETE){
                    storageService.deleteFile(filepath);
                }
            }
        }
    }

    private  boolean checkUserOwnership(String filepath){
        String currentUserId=authenticatedUserService.getcurrentuser();
        int first=filepath.indexOf("/");
        int last=filepath.lastIndexOf("/");
        String providedUserId=filepath.substring(first+1,last);
        return providedUserId.equals(currentUserId);
    }

}
