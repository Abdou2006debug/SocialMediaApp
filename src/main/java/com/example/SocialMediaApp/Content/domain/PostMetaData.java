package com.example.SocialMediaApp.Content.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class PostMetaData {
    private List<Media> media;
    private String locationName;
    private Double latitude;
    private Double longitude;
}
