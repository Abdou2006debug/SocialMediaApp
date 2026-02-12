package com.example.SocialMediaApp.Content.api.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class uploadRequest {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadType;
    private Integer duration; // Optional
}
