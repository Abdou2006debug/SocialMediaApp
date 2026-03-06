package com.example.SocialMediaApp.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public class UploadFailedResponse {
    private String message;
    private List<String> failedUploadIds;
}
