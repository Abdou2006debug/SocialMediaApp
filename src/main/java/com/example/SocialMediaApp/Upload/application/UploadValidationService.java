package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Shared.Exceptions.FileTooLargeException;
import com.example.SocialMediaApp.Shared.Exceptions.UnsupportedMediaTypeException;
import com.example.SocialMediaApp.Shared.Exceptions.UploadTypeMismatch;
import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.SocialMediaApp.Upload.domain.UploadType.*;


@Component
 class UploadValidationService {

    static final Map<UploadType,List<String>> allowedTypes=Map.of(
            PROFILE, Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"),
            STORY, Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/quicktime" ),
            POST, Arrays.asList(
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

    public void validateFile(UploadRequest request){
     List<String> allowedTypesForRequest =allowedTypes.get(request.getUploadType());
     boolean compatible=false;
     if(allowedTypesForRequest!=null){
        compatible= allowedTypesForRequest.stream().anyMatch(allowedTypes->allowedTypes.equals(request.getFileMimeType().toLowerCase()));
     }

        String filetMimeType =request.getFileMimeType();

     if(compatible){
         boolean isVideo =  filetMimeType.startsWith("video/");
         long limit=switch (request.getUploadType()) {

             case PROFILE -> MAX_PROFILE_SIZE;


             case POST -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_POST_IMAGE_SIZE;


             case STORY -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_STORY_IMAGE_SIZE;
         };

         if(limit<request.getFileSize()){
             throw new FileTooLargeException("file size is too large");
         }

         return;

     }

     throw new UnsupportedMediaTypeException("file type is unsupported");
    }

    // this method will confirm that the upload type the user wants to create match the filepath upload type
    public void confirmUploadType(String filepath, UploadType intendedType){
        boolean compatibleType = filepath.split("/")[1].
                equals(intendedType.toString().toLowerCase());
        if(!compatibleType) throw new UploadTypeMismatch("");
    }


}
