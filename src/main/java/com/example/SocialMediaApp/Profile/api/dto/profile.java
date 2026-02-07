package com.example.SocialMediaApp.Profile.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class profile {

    @Size(max = 30)
    @JsonProperty("bio")
    private String bio;
    private String username;
}
