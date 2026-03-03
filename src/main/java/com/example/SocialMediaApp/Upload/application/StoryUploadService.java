package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Upload.api.dto.StoryUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.StoryUploadResponse;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryUploadService {

    private final MediaUploadService mediaUploadService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String,Object> objectRedisTemplate;
    private final StorageService storageService;



    public StoryUploadResponse requestStoryUpload(String userId, StoryUploadRequest storyUploadRequest){
        String batchId= storyUploadRequest.getBatchId();
        Map<Object,Object> batchMap=redisTemplate.opsForHash().entries(batchId);
        String actualUserId=(String) batchMap.get("userId");
        if(actualUserId==null||!actualUserId.equals(userId)) throw new ActionNotAllowedException("Action could not be completed");
        UploadSession uploadSession=mediaUploadService.requestUpload(userId,storyUploadRequest);
        uploadSession.setUploadType(UploadType.STORY);
        uploadSession.setBatchId(batchId);
        uploadSession.setIndex(storyUploadRequest.getIndex());
        objectRedisTemplate.opsForValue().set(uploadSession.getFilePath(),uploadSession);
        return new StoryUploadResponse(uploadSession.getSignedUrl());
    }

    public List<Media> finalizeStoryUploads(String userId, String batchId){
       Map<Object,Object> batchMap=redisTemplate.opsForHash().entries(batchId);
       String actualUserId=(String) batchMap.get("userId");
       if(actualUserId==null||!actualUserId.equals(userId)) throw new ActionNotAllowedException("");
      List<Media> mediaList= batchMap.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith("file:"))
                .map(e -> {
                    String filePath=e.getKey().toString().replace("file:permanent", "");
                    Media.MediaType mediaType=(Media.MediaType) e.getValue();
                    return new Media(filePath,mediaType);
                }).toList();

       storageService.moveFilesToPermanent(filePaths);
        return mediaList;
    }


    public String requestStoryBatch(String userId){
        String batchId= UUID.randomUUID().toString();
        redisTemplate.opsForHash().put(batchId, "userId", userId);
        redisTemplate.expire(batchId, 20, TimeUnit.MINUTES);
        return batchId;
    }

}
