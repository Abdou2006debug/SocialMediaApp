package com.example.SocialMediaApp.Content.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class PostMetaData {
    private List<String> images;
    private List<String> videos;
    private String locationName;
    private Double latitude;
    private Double longitude;
}
