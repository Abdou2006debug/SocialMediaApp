package com.example.SocialMediaApp.Upload.api.dto;

import com.example.SocialMediaApp.Upload.domain.UploadType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UploadRequest {
    private String fileName;
    private String fileMimeType;
    private Long fileSize;
    private UploadType uploadType;
    @Max(15)
    @Min(1)
    private Integer duration; // Optional

    public UploadRequest(String fileMimeType,Long fileSize,UploadType uploadType){
        this.fileMimeType=fileMimeType;
        this.fileSize=fileSize;
        this.uploadType=uploadType;
    }

}
