package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Storage.uploadType;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class uploadRequest {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private uploadType uploadType;
    private Integer duration; // Optional
}
