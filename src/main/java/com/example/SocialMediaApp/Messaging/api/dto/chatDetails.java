package com.example.SocialMediaApp.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class chatDetails {
    private String chatId;
    private chatUser chatUser;
    private List<messageDTO> messages;
}
