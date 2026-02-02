package com.example.whatsappclone.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class chatDTO {
    private String chatId;
    private String chatView;
    private String userId;
    private String username;
    private String avatarurl;
}
