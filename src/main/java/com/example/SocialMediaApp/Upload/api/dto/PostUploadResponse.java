package com.example.SocialMediaApp.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class PostUploadResponse extends BaseUploadResponse{
    private String requestId;
    public PostUploadResponse(String signedUrl,String requestId){
        super(signedUrl);
        this.requestId=requestId;
    }
}
