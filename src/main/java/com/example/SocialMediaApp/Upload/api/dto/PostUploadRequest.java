package com.example.SocialMediaApp.Upload.api.dto;

import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PostUploadRequest extends BaseUploadRequest {

    public PostUploadRequest( String fileName,String fileMimeType,Long fileSize,Integer duration){
        super(fileName,fileMimeType,fileSize,duration);
    }
}
