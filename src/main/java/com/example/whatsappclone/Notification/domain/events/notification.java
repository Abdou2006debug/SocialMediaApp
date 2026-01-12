package com.example.whatsappclone.Notification.domain.events;

import com.example.whatsappclone.User.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class notification {
    private User trigger;
    private User recipient;
    private notificationType type;

    public enum notificationType{FOLLOW,FOLLOW_REQUESTED,FOLLOWING_ACCEPTED,FOLLOWING_REJECTED}
}
