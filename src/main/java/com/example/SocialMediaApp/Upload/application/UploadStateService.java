package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;

import com.example.SocialMediaApp.Upload.domain.UploadPhase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
 class UploadStateService {

    private final RedisTemplate<String,String> redisTemplate;

    public String validateUploadSession(String userId, String uploadRequestId, UploadPhase uploadPhase) {
        String key=String.format("%s:%s",uploadPhase.toString().toLowerCase(),uploadRequestId);

        String filepath=redisTemplate.opsForValue().get(key);

        if(filepath==null) throw new ActionNotAllowedException("Upload Session expired or Invalid");

        if(userId!=null&&!checkUserOwnership(userId,filepath)){
            // logging later
            throw new ActionNotAllowedException("Action could not be completed");
        }

        return filepath;
    }

    private  boolean checkUserOwnership(String userId,String filepath){
        String providedUserId=filepath.split("/")[2];
        return providedUserId.equals(userId);
    }

}
