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
    // specific for posts
    private String uploadRequestId;
    //specific for stories
    private String batchId;
    private Integer index;

    private String signedUrl;
    private String filePath;
    private Media.MediaType mediaType;
}
