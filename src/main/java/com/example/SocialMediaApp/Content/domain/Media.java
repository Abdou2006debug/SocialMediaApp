package com.example.SocialMediaApp.Content.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Media {
    private String filepath;
    private MediaType mediaType;

    public enum MediaType {
        IMAGE,VIDEO
    }

}
