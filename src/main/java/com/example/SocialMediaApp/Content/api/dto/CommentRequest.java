package com.example.SocialMediaApp.Content.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CommentRequest {
    @NotBlank
    @Size(max = 500)
    private String content;
}
