package com.example.SocialMediaApp.Messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class sendMessageToChatDTO {
    @NotBlank
    private String chatId;
    @NotBlank
    private String content;
}
