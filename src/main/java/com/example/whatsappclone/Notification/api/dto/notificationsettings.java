package com.example.whatsappclone.Notification.api.dto;

import jakarta.persistence.Column;
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
