package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Exceptions.UploadFailedException;
import com.example.SocialMediaApp.Shared.Exceptions.UploadTypeMismatch;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Upload.api.dto.PostUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.PostUploadResponse;
import com.example.SocialMediaApp.Upload.domain.UploadPhase;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostUploadService {

    private final MediaUploadService uploadGatewayService;
    private final UploadStateService uploadStateService;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final RedisTemplate<String,String> redisTemplate;
    private final StorageService storageService;

    public PostUploadResponse requestPostUpload(String userId, PostUploadRequest postUploadRequest){
        UploadSession uploadSession=uploadGatewayService.requestUpload(userId,postUploadRequest);
        uploadSession.setUploadType(UploadType.POST);
        objectRedisTemplate.opsForValue().set(uploadSession.getFilePath(),uploadSession);
        return new PostUploadResponse(uploadSession.getSignedUrl(),uploadSession.getUploadRequestId());
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
            }catch (ActionNotAllowedException | UploadTypeMismatch e){
                failedUploadIds.add(uploadRequestId);
            }
        }

        if(!failedUploadIds.isEmpty()) throw new UploadFailedException(failedUploadIds);

        storageService.moveFilesToPermanent(filesPaths);

        redisTemplate.delete(uploadRequestsIds);

        return mediaList;

    }
}
