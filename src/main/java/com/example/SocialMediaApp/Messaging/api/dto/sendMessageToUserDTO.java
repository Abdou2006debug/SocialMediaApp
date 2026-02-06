package com.example.SocialMediaApp.Messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class sendMessageToUserDTO {
    @NotBlank
    private String userId;
    @NotBlank
    private String content;

}
