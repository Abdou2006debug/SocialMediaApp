package com.example.whatsappclone.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class chatUser {
    private String userId;
    private String username;
    private String avatarurl;
    private Boolean online;
    private String lastActivity;
}
