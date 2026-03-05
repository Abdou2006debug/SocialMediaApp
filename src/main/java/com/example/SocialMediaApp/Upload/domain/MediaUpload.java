package com.example.SocialMediaApp.Upload.domain;

import com.example.SocialMediaApp.Content.domain.Media;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MediaUpload {
    private String filePath;
    private Media.MediaType mediaType;
}
