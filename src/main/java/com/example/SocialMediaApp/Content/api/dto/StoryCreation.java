package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.StorySettings;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StoryCreation {
    @NotBlank
    private String uploadRequestId;
    private StorySettings storySettings;
}
