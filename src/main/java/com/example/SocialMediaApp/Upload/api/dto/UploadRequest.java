package com.example.SocialMediaApp.Upload.api.dto;

import com.example.SocialMediaApp.Upload.domain.uploadType;
import lombok.Data;

@Data
public class UploadRequest {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private uploadType uploadType;
    private Integer duration; // Optional
}
