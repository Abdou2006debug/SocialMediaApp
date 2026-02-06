package com.example.SocialMediaApp.Notification.application;
import com.example.SocialMediaApp.Notification.api.dto.notification;
import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.example.SocialMediaApp.User.application.UserActivityService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Notification.persistence.NotificationSettingsRepo;
import com.example.SocialMediaApp.Profile.application.ProfileQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowNotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final ProfileQueryService profileQueryService;
    private final UserActivityService userActivityService;

    @Async
    @EventListener
    public void FollowNotificationProcessing(FollowNotification notificationEvent) {

        User recipient= notificationEvent.getRecipient();
        boolean Send= canSendNotification(recipient.getUuid(),notificationEvent.getType());
        if(!Send){
            return;
        }

        User trigger=notificationEvent.getTrigger();
        ProfileInfo profileInfo=profileQueryService.getUserProfileInfo(trigger.getUuid());
        StringBuilder message=new StringBuilder(profileInfo.getUsername());
        switch(notificationEvent.getType()){
            case FOLLOW->message.append(" Started Following you");
            case FOLLOW_REQUESTED -> message.append(" Requested Following you");
            case FOLLOWING_ACCEPTED -> message.append(" Accepted Your follow");
            case FOLLOWING_REJECTED -> message.append(" Rejected Your follow");
        }
        log.info("publishing "+message.toString() +" to "+recipient.getUuid());
        notification notification=new notification(message.toString(),profileInfo.getAvatarurl(),profileInfo.getUserId());
        simpMessagingTemplate.convertAndSendToUser(recipient.getUuid(),"/queue/notifications",notification);
    }

    private boolean canSendNotification(String userId, FollowNotification.notificationType notificationType){
       boolean Online= userActivityService.getUserStatus(userId);
       if(!Online){
           log.error("user "+userId+" is not online ");
           return false;
       }

       NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUser(new User(userId));
       switch (notificationType){
           case FOLLOW,FOLLOW_REQUESTED-> {
               if(!notificationsSettings.getOnfollow()){
                   return false;
               }
           }
           case FOLLOWING_ACCEPTED->{
                   if(!notificationsSettings.getOnfollowingrequestAccepted()){
                       return false;
                   }
               }
           case FOLLOWING_REJECTED -> {
               if(!notificationsSettings.getOnfollowingrequestRejected()){
                   return false;
               }
           }
       }
       return true;
    }

}
