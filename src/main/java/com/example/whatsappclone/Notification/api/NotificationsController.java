package com.example.whatsappclone.Notification.api;

import com.example.whatsappclone.Notification.api.dto.notificationsettings;
import com.example.whatsappclone.Notification.application.NotificationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationSettingsService notificationSettingsService;

    @PutMapping("/settings")
    public void updateNotificationSettings(@RequestBody notificationsettings settings) {
        notificationSettingsService.updateNotificationSettings(settings);
    }

@GetMapping("/settings")
public notificationsettings getNotificationSettings(){
        return notificationSettingsService.getnotificationsettings();
}

}

