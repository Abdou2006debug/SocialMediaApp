package com.example.SocialMediaApp.Notification.application;

import com.example.SocialMediaApp.Shared.Mappers.Profilemapper;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Notification.api.dto.notificationsettings;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Notification.persistence.NotificationSettingsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final NotificationSettingsRepo notificationSettingsRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final Profilemapper profilemapper;
    public void updateNotificationSettings(notificationsettings notification){
        User currentuser=authenticatedUserService.getcurrentuser();
        NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(currentuser);
        notificationsSettings.setOnfollow(notification.getOnfollow());
        notificationsSettings.setOnfollowingrequestRejected(notification.getOnfollowingrequestRejected());
        notificationsSettings.setOnfollowingrequestAccepted(notification.getOnfollowingrequestAccepted());
        notificationSettingsRepo.save(notificationsSettings);
    }

    public notificationsettings getnotificationsettings(){
        User currentuser=authenticatedUserService.getcurrentuser();
        NotificationsSettings notificationsSettings= notificationSettingsRepo.findByUser(currentuser);
        return profilemapper.tonotificationsettings(notificationsSettings);
    }

}
