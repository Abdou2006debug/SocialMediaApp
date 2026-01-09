package com.example.whatsappclone.Notification.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Notification.api.dto.notificationsettings;
import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import com.example.whatsappclone.Notification.persistence.NotificationSettingsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final NotificationSettingsRepo notificationSettingsRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public void updateNotificationSettings(notificationsettings notification){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(currentuser);
        notificationsSettings.setOnfollow(notification.isOnfollower());
        notificationsSettings.setOnfollowingrequestRejected(notification.isOnfollowingrequests_rejected());
        notificationsSettings.setOnfollowingrequestAccepted(notification.isOnfollowingrequests_accepted());
        notificationSettingsRepo.save(notificationsSettings);
    }

    public notificationsettings getnotificationsettings(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        NotificationsSettings notificationsSettings= notificationSettingsRepo.findByUser(currentuser);
        return new notificationsettings(notificationsSettings.getOnfollowingrequestAccepted(),notificationsSettings.getOnfollowingrequestRejected(),notificationsSettings.getOnfollow());

    }

}
