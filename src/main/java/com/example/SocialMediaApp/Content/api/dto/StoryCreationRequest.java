package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.StorySettings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class StoryCreationRequest {
    @Size(min = 1,max = 10)
    private List<String> uploadRequestsIds;
    private StorySettings storySettings;
}
