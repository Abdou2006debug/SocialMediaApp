package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Upload.api.dto.BaseUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.PostUploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.StoryUploadRequest;
import com.example.SocialMediaApp.Upload.domain.UploadInitiation;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.example.SocialMediaApp.Upload.domain.UploadType.*;

@Service
@RequiredArgsConstructor
 class UploadUtil {

    public UploadInitiation generateUploadResponse(String userId, BaseUploadRequest request){
        String uploadRequestId = UUID.randomUUID().toString();
        UploadType uploadType=request instanceof PostUploadRequest ? POST:request instanceof StoryUploadRequest ?STORY:PROFILE;
        return new UploadInitiation(String.format("temporary/%s/%s/%s",uploadType.toString().toLowerCase(),userId,uploadRequestId),uploadRequestId);
    }

    public PostUploadRequest toUploadRequest(MultipartFile file){
        PostUploadRequest request = new PostUploadRequest();
        request.setFileName(file.getName());
        request.setFileMimeType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType(UploadType.PROFILE);
        return request;
    }
}

