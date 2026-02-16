package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.upload;
import com.example.SocialMediaApp.Storage.uploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UploadGatewayService {

    private final StorageService storageService;
    private final RedisTemplate<String,String> redisTemplate;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 10;

    public String requestUpload(String userId,uploadRequest uploadRequest){
        upload upload=storageService.generateSignedUrl(uploadRequest,userId);
        String filepath=upload.getFilepath();
        redisTemplate.opsForValue().set(filepath,"waiting",UPLOAD_WAIT_DURATION_MINUTES, TimeUnit.MINUTES);
        return upload.getSigneduri();
    }

    public void confirmUpload(String userId,String filepath){
        if(checkUserOwnership(userId,filepath)){
            String status=redisTemplate.opsForValue().get(filepath);
            if(status==null||!status.equals("waiting")){
                throw new RuntimeException("Upload Session expired or Invalid");
            }
            boolean fileExist=storageService.checkFileExist(filepath);
            if(!fileExist){
                throw new RuntimeException("Upload failed try again");
            }
            redisTemplate.opsForValue().set(filepath,"uploaded",UPLOAD_CONFIRM_DURATION_MINUTES,TimeUnit.MINUTES);
            storageService.scheduleCleanSupabase(filepath,UPLOAD_CONFIRM_DURATION_MINUTES);
        }
    }

    public void deleteUpload(String userId,String filepath){

        if(!checkUserOwnership(userId,filepath)) throw new RuntimeException();

        String status=redisTemplate.opsForValue().get(filepath);

        if(status==null||!status.equals("uploaded")) throw new RuntimeException();

        redisTemplate.delete(filepath);
        storageService.cancelScheduledClean(filepath);
    }

    public void finalizeUploads(String userId, List<String> filespaths,uploadType uploadType){
        for(String filepath:filespaths){

        if(!checkUserOwnership(userId,filepath)) throw new RuntimeException();

        String status=redisTemplate.opsForValue().get(filepath);

        if(status==null||!status.equals("uploaded")) throw new RuntimeException();

        }

        // if we get here that means all files are valid and we untrack them
        filespaths.forEach(filepath->{
            redisTemplate.delete(filepath);
            storageService.cancelScheduledClean(filepath);
        });

            }

    // this method will confirm that the upload type the user wants to create match the filepath upload type
    public boolean confirmType(String filepath, uploadType intendedType){
        int first=filepath.indexOf("/");
        String actualType=filepath.substring(0,first);
        return actualType.equals(intendedType.toString());
    }

    private  boolean checkUserOwnership(String userId,String filepath){
        int first=filepath.indexOf("/");
        int last=filepath.lastIndexOf("/");
        String providedUserId=filepath.substring(first+1,last);
        return providedUserId.equals(userId);
    }

}
