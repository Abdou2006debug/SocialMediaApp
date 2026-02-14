package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Media;
import lombok.Getter;

import java.util.List;

@Getter
public class postCreation {
    private String caption;
    private List<Media> mediaList;
    private boolean commentDisabled;
    private boolean showLikes;
}
