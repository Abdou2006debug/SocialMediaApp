package com.example.SocialMediaApp.Profile.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class profile {

    @Size(max = 30)
    private String bio;
    @NotBlank
    private String username;
}
