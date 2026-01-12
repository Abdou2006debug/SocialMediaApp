package com.example.whatsappclone.User.application;

import com.example.whatsappclone.User.api.dto.userregistration;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import com.example.whatsappclone.Notification.persistence.NotificationSettingsRepo;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Mappers.Usermapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final Usermapper usermapper;
    private final IdentityService identityService;


    // method responsible for creating the User and initializing profile and notification settings
    // transactional is none negotiable because user provisioning might not happen so the user shouldn't exists in the db
    @Transactional
    public void registerUser(userregistration userregistration){
        User user=userRepo.save(usermapper.toUserentity(userregistration));
        Profile profile=new Profile(null,userregistration.getUsername());
        profile.setUser(user);
        NotificationsSettings notificationsSettings=NotificationsSettings.builder().
                user(user).Onfollow(true).onfollowingrequestRejected(true).onfollowingrequestAccepted(true).build();
        notificationSettingsRepo.save(notificationsSettings);
        profileRepo.save(profile);
        identityService.UserProvision(userregistration,user.getUuid());
    }

}
