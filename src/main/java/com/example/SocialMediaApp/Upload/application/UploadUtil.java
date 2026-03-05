package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
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

    public UploadInitiation generateUploadResponse(String userId, UploadRequest request){
        String uploadRequestId = UUID.randomUUID().toString();
        UploadType uploadType=request.getUploadType();
        return new UploadInitiation(String.format("temporary/%s/%s/%s",uploadType.toString().toLowerCase(),userId,uploadRequestId),uploadRequestId);
    }

    public UploadRequest toUploadRequest(MultipartFile file){
        UploadRequest request = new UploadRequest();
        request.setFileMimeType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType(UploadType.PROFILE);
        return request;
    }
}

