package com.example.SocialMediaApp.Upload.domain;

import com.example.SocialMediaApp.Content.domain.Media;
import lombok.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class UploadSession {
    private String userId;
    private UploadType uploadType;
    private String uploadRequestId;
    private String filePath;
    private Media.MediaType mediaType;
}
