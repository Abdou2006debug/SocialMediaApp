package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Shared.Exceptions.FileOwnershipException;
import com.example.SocialMediaApp.Upload.domain.uploadPhase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
 class UploadStateService {

    private final RedisTemplate<String,String> redisTemplate;

    public String validateUploadSession(String userId, String uploadRequestId, uploadPhase uploadPhase){
        String key=String.format("%s:%s",uploadPhase.toString().toLowerCase(),uploadRequestId);

        String filepath=redisTemplate.opsForValue().get(key);

        if(filepath==null) throw new IllegalArgumentException("Upload Session expired or Invalid");

        if(!checkUserOwnership(userId,filepath)) throw new FileOwnershipException("Access Denied");

        return filepath;
    }

    private  boolean checkUserOwnership(String userId,String filepath){
        int first=filepath.indexOf("/");
        int last=filepath.lastIndexOf("/");
        String providedUserId=filepath.substring(first+1,last);
        return providedUserId.equals(userId);
    }

}
