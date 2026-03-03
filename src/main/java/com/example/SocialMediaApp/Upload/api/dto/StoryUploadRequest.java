package com.example.SocialMediaApp.Upload.api.dto;

import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class StoryUploadRequest extends BaseUploadRequest {
    @NotBlank
    private String batchId;
    private Integer index;
    public StoryUploadRequest(String batchId, String fileName, Integer index,String fileMimeType,Long fileSize,Integer duration){
        super(fileName,fileMimeType,fileSize,duration);
        this.batchId=batchId;
        this.index=index;
    }
}
