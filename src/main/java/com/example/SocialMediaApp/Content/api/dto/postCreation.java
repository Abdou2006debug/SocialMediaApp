package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Media;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class postCreation {
    private String caption;
    @Size(min = 1, max = 10)
    private List<String> filepaths;
    private List<String> tags;
    private boolean commentsDisabled;
    private boolean hideLikes;
}
