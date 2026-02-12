package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.api.dto.uploadRequest;
import com.example.SocialMediaApp.Shared.Exceptions.FileTooLargeException;
import com.example.SocialMediaApp.Shared.Exceptions.UnsupportedMediaTypeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
 class StorageUtil {

    static final Map<String,List<String>> allowedTypes=Map.of(
            "profile", Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"),
            "story", Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/quicktime" ),
            "post", Arrays.asList(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "video/mp4",
                    "video/mpeg",
                    "video/quicktime"
            )
            );

    public static final long MAX_PROFILE_SIZE = 100 * 1024L;

    public static final long MAX_STORY_IMAGE_SIZE = 500 * 1024L;

    public static final long MAX_POST_IMAGE_SIZE = 1024 * 1024L;

    public static final long MAX_VIDEO_SIZE = 30 * 1024 * 1024L;

    public String generateFilePath(uploadRequest request,String userId){
        String uuid= UUID.randomUUID().toString();
        return String.format("%s/%s/%s", request.getUploadType().toLowerCase(), userId, uuid);
    }

    public void validateRequest(uploadRequest request){
     List<String> allowedTypesForRequest =allowedTypes.get(request.getUploadType().toLowerCase());
     boolean compatible= allowedTypesForRequest.stream().anyMatch(allowedTypes->allowedTypes.equals(request.getFileType()));
     String filetype=request.getFileType();

     boolean isVideo = filetype!= null && filetype.startsWith("video/");

     if(compatible){
         long limit=switch (request.getUploadType().toLowerCase()) {

             case "profile" -> MAX_PROFILE_SIZE;


             case "post" -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_POST_IMAGE_SIZE;

             case "story" -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_STORY_IMAGE_SIZE;

             default -> throw new IllegalArgumentException("Unknown category: " + filetype);
         };
         if(limit<request.getFileSize()){
             throw new FileTooLargeException("file size is too large");
         }
         return;
     }
     throw new UnsupportedMediaTypeException("file type is unsupported");
    }

    public uploadRequest toUploadRequest(MultipartFile file){
        uploadRequest request = new uploadRequest();
        request.setFileName(file.getName());
        request.setFileType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType("Profile");
        return request;
    }

}
