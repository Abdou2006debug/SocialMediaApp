package com.example.SocialMediaApp.Notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class notificationsettings {
    private Boolean onfollowingrequestAccepted;
    private Boolean onfollowingrequestRejected;
    private Boolean Onfollow;
}
