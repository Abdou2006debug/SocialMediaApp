package com.example.SocialMediaApp.Content.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PostCreation {
    private String caption;
    @Size(min = 1, max = 10)
    private List<String> uploadRequestsIds;
    private List<String> tags;
    private boolean commentsDisabled;
    private boolean likesDisabled;
}
