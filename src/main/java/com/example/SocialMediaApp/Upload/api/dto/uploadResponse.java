package com.example.SocialMediaApp.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class uploadResponse {
    private String signedUrl;
    private String requestId;
}
