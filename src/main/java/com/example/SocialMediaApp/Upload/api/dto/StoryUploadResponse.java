package com.example.SocialMediaApp.Upload.api.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StoryUploadResponse extends BaseUploadResponse{
    public StoryUploadResponse(String signedUrl){
        super(signedUrl);
    }
}
