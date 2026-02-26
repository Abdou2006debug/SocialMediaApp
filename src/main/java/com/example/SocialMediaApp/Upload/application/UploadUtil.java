package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
 class UploadUtil {

    public Map<String,String> generateUploadResponse(String userId, UploadRequest request){
        String uploadRequestId = UUID.randomUUID().toString();

        Map<String,String> uploadMap=new HashMap<>();
        String mediaCategory = request.getFileMimeType().startsWith("video/") ? "video" : "image";
        uploadMap.put("filepath",String.format("temporary/%s/%s/%s/%s",request.getUploadType().toString().toLowerCase(),userId,mediaCategory,uploadRequestId));
        uploadMap.put("uploadRequestId",uploadRequestId);
        return uploadMap;
    }

    public List<Media> convertToMedia(List<String> filepaths){
        return filepaths.stream().map(filepath->{
            String type=filepath.split("/")[3];
            Media.MediaType mediaType= type.equalsIgnoreCase("video")?
                    Media.MediaType.VIDEO: Media.MediaType.IMAGE;
            return new Media(filepath.replace("temporary/",""),mediaType);
        }).toList();
    }

    public UploadRequest toUploadRequest(MultipartFile file){
        UploadRequest request = new UploadRequest();
        request.setFileName(file.getName());
        request.setFileMimeType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType(UploadType.PROFILE);
        return request;
    }
}

