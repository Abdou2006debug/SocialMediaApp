package com.example.whatsappclone.Notification.application;
import com.example.whatsappclone.Notification.api.dto.notification;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import com.example.whatsappclone.Notification.persistence.NotificationSettingsRepo;
import com.example.whatsappclone.Profile.application.ProfileQueryService;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CacheWriterService cachService;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final ProfileQueryService profileQueryService;
    private final Logger logger= LoggerFactory.getLogger(NotificationService.class);


    private void follownotification(com.example.whatsappclone.Notification.domain.events.notification notification) throws JsonProcessingException {

        User recipient=notification.getRecipient();
        NotificationsSettings Settings=notificationSettingsRepo.findByUser(recipient);
        if(!cachService.getuserstatus(recipient.getUsername())){
            //push as web notification if enabled;
            logger.info(recipient.getUsername()+" is not online");
            return;
        }
        if(!Settings.getOnfollow()){
            return ;
        }
        User trigger=notification.getTrigger();
        Profile triggerprofile=profileQueryService.getuserprofile(trigger,true);
        String triggerusername=triggerprofile.getUuid();
        String triggerpfp=triggerprofile.getPublicavatarurl();
        String triggeruuid=trigger.getUuid();
        String message;
        if(notification.getType().equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOW)){
            message=triggerusername+" started following you";
        }else{
            message=triggerusername+" requested to follow you";
        }
            notification notification1=new notification(message,triggerpfp,triggeruuid);
        sendnotification(recipient,notification1);
    }
    private void followingnotification(com.example.whatsappclone.Notification.domain.events.notification notification) throws JsonProcessingException {
        User recipient=notification.getRecipient();
        NotificationsSettings Settings=notificationSettingsRepo.findByUser(recipient);
        if(!cachService.getuserstatus(recipient.getUsername())){
            //push as web notification if enabled;
            logger.info(recipient.getUsername()+" is not online");
            return;
        }
        String message;
        User trigger=notification.getTrigger();
        if(notification.getType().equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOWING_ACCEPTED)){
            if(!Settings.getOnfollowingrequestAccepted()){
                logger.info(recipient.getUsername()+" has disabled the following accepted notifications");
                return ;
            }
            message=trigger.getUsername()+" accepted your follow";
        }else{
            if(!Settings.getOnfollowingrequestRejected()){
                logger.info(recipient.getUsername()+" has disabled the following rejected notifications");
                return;
            }
            message=trigger.getUsername()+" rejected your follow";
        }
        Profile triggerprofile= profileQueryService.getuserprofile(trigger,false);
        String triggerusername=triggerprofile.getUsername();
        String triggerpfp=triggerprofile.getPublicavatarurl();
        String triggeruuid=trigger.getUuid();
        notification notification1=new notification(message,triggerpfp,triggeruuid);
        sendnotification(recipient,notification1);
    }

    @EventListener
    @Async
    public void handleNotificationEvents(com.example.whatsappclone.Notification.domain.events.notification n) {
       com.example.whatsappclone.Notification.domain.events.notification.notificationType notificationType=n.getType();
        if(notificationType.equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOW)||
        notificationType.equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOW_REQUESTED)){
            logger.info("resolving follow event to "+n.getRecipient().getUsername());
            try {
                follownotification(n);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if(notificationType.equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOWING_ACCEPTED)||
                notificationType.equals(com.example.whatsappclone.Notification.domain.events.notification.notificationType.FOLLOWING_REJECTED)){
            logger.info("resolving following event to "+n.getRecipient().getUsername());
            try {
                followingnotification(n);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

   private void sendnotification(User recipient,notification  notification) throws JsonProcessingException {
        ObjectMapper mapper=new ObjectMapper();
       logger.info("sending notification to "+recipient.getUuid());
        logger.info(recipient.getUsername());
        simpMessagingTemplate.convertAndSendToUser(recipient.getUuid(),"/queue/notifications",mapper.writeValueAsString(notification));
    }
}
