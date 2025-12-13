package com.example.whatsappclone.Services;

import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.notification;
import com.example.whatsappclone.Repositries.NotificationSettingsRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
//import com.example.whatsappclone.DTO.serverToclient.notification;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CachService cachService;
    private final ProfileRepo profileRepo;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final UsersManagmentService usersManagmentService;
    private final Logger logger= LoggerFactory.getLogger(NotificationService.class);
    private void follownotification(notification notification) throws JsonProcessingException {
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
        Profile triggerprofile=usersManagmentService.getuserprofile(trigger,true);
        String triggerusername=triggerprofile.getUsername();
        String triggerpfp=triggerprofile.getPublicavatarurl();
        String triggeruuid=trigger.getUuid();
        String message;
        if(notification.getType().equals(com.example.whatsappclone.Events.notification.notificationType.FOLLOW)){
            message=triggerusername+" started following you";
        }else{
            message=triggerusername+" requested to follow you";
        }
            com.example.whatsappclone.DTO.serverToclient.notification notification1=new com.example.whatsappclone.DTO.serverToclient.notification(message,triggerpfp,triggeruuid,notification.getFollowid());
        sendnotification(recipient,notification1);
    }
    private void followingnotification(notification notification) throws JsonProcessingException {
        User recipient=notification.getRecipient();
        NotificationsSettings Settings=notificationSettingsRepo.findByUser(recipient);
        if(!cachService.getuserstatus(recipient.getUsername())){
            //push as web notification if enabled;
            logger.info(recipient.getUsername()+" is not online");
            return;
        }
        String message;
        User trigger=notification.getTrigger();
        if(notification.getType().equals(com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_ACCEPTED)){
            if(!Settings.getOnfollowingrequest_Accepted()){
                logger.info(recipient.getUsername()+" has disabled the following accepted notifications");
                return ;
            }
            message=trigger.getUsername()+" accepted your follow";
        }else{
            if(!Settings.getOnfollowingrequest_rejected()){
                logger.info(recipient.getUsername()+" has disabled the following rejected notifications");
                return;
            }
            message=trigger.getUsername()+" rejected your follow";
        }
        Profile triggerprofile=usersManagmentService.getuserprofile(trigger,false);
        String triggerusername=triggerprofile.getUsername();
        String triggerpfp=triggerprofile.getPublicavatarurl();
        String triggeruuid=trigger.getUuid();
        com.example.whatsappclone.DTO.serverToclient.notification notification1=new com.example.whatsappclone.DTO.serverToclient.notification(message,triggerpfp,triggeruuid,notification.getFollowid());
        sendnotification(recipient,notification1);
    }
    @EventListener
    @Async
    public void handleNotificationEvents(notification n) {
       notification.notificationType notificationType=n.getType();
        if(notificationType.equals(com.example.whatsappclone.Events.notification.notificationType.FOLLOW)||
        notificationType.equals(com.example.whatsappclone.Events.notification.notificationType.FOLLOW_REQUESTED)){
            logger.info("resolving follow event to "+n.getRecipient().getUsername());
            try {
                follownotification(n);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if(notificationType.equals(com.example.whatsappclone.Events.notification.notificationType.FOLLOWING_ACCEPTED)||
                notificationType.equals(notification.notificationType.FOLLOWING_REJECTED)){
            logger.info("resolving following event to "+n.getRecipient().getUsername());
            try {
                followingnotification(n);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
   private void sendnotification(User recipient,com.example.whatsappclone.DTO.serverToclient.notification  notification) throws JsonProcessingException {
        ObjectMapper mapper=new ObjectMapper();
       logger.info("sending notification to "+recipient.getUsername());
        logger.info(recipient.getUsername());
        simpMessagingTemplate.convertAndSendToUser(recipient.getUsername(),"/queue/notifications",mapper.writeValueAsString(notification));
    }
}
