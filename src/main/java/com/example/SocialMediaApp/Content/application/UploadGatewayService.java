package com.example.SocialMediaApp.Content.application;

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
        storageService.scheduleCleanSupabase(filepath,5);
        return upload.getSigneduri();
    }

    public void confirmUpload(uploadConfirm uploadConfirm){
        checkUserOwnership(uploadConfirm);
        String filepath=uploadConfirm.getFilepath();
        String status=redisTemplate.opsForValue().get(filepath);
        if(status==null||!status.equals("waiting")){
            throw new RuntimeException("Upload Session expired or Invalid");
        }
        boolean fileExist=storageService.checkFileExist(filepath);
        if(!fileExist){
            throw new RuntimeException("Upload failed try again");
        }
        redisTemplate.opsForValue().set(filepath,"uploaded",10,TimeUnit.MINUTES);
        storageService.cancelScheduledClean();
        storageService.scheduleCleanSupabase(filepath,10);
    }

    public void deleteUploaded(uploadConfirm uploadConfirm){
        checkUserOwnership(uploadConfirm);
        String filepath=uploadConfirm.getFilepath();
        redisTemplate.delete(uploadConfirm.getFilepath());
        storageService.cancelScheduledClean();
        storageService.deleteFile(filepath);
    }

    private  void checkUserOwnership(uploadConfirm uploadConfirm){
        String currentUserId=authenticatedUserService.getcurrentuser();
        String filepath=uploadConfirm.getFilepath();
        int first=filepath.indexOf("/");
        int last=filepath.lastIndexOf("/");
        String providedUserId=filepath.substring(first+1,last);
        if(!providedUserId.equals(currentUserId)){
            throw  new RuntimeException();
        }
    }

}
