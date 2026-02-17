package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Upload.api.dto.uploadRequest;
import com.example.SocialMediaApp.Upload.domain.uploadType;
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

    public Map<String,String> generateUploadResponse(String userId, uploadRequest request){
        String requestId= UUID.randomUUID().toString();
        String filetype=request.getFileType();
        int first=filetype.indexOf("/");
        String type=request.getFileType().substring(0,first);
        Map<String,String> uploadMap=new HashMap<>();
        uploadMap.put("filepath",String.format("%s/%s/%s/%s",request.getUploadType(),userId,type,requestId));
        uploadMap.put("requestId",requestId);
        return uploadMap;
    }
    public List<Media> convertToMedia(List<String> filepaths){
        return filepaths.stream().map(filepath->{
            int last=filepath.lastIndexOf("/");
            String type=filepath.substring(last);
            Media.MediaType mediaType= type.equalsIgnoreCase("video")?
                    Media.MediaType.VIDEO: Media.MediaType.IMAGE;
            return new Media(filepath,mediaType);
        }).collect(Collectors.toList());
    }
    public uploadRequest toUploadRequest(MultipartFile file){
        uploadRequest request = new uploadRequest();
        request.setFileName(file.getName());
        request.setFileType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType(uploadType.PROFILE);
        return request;
    }
}

