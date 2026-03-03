package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.StorySettings;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StoryCreationRequest {
    @NotBlank
    private String batchId;
    private StorySettings storySettings;
}
