package com.example.SocialMediaApp.User.application;

import com.example.SocialMediaApp.Shared.Exceptions.UserRegistrationException;
import com.example.SocialMediaApp.User.api.dto.userregistration;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Notification.persistence.NotificationSettingsRepo;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.Mappers.Usermapper;
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



    @Transactional
    public void registerUser(userregistration userregistration){
        String userId=identityService.UserProvision(userregistration);
        User user=usermapper.toUserentity(userregistration);
        user.setId(userId);
        try{
            userRepo.saveAndFlush(user);
            Profile profile=new Profile(null,userregistration.getUsername());
            profile.setUser(user);
            NotificationsSettings notificationsSettings=NotificationsSettings.builder().
                    user(user).Onfollow(true).onfollowingrequestRejected(true).onfollowingrequestAccepted(true).build();
            notificationSettingsRepo.save(notificationsSettings);
            profileRepo.save(profile);
        }catch (Exception e){
            identityService.UserRemoval(userId);
          log.error("failed to save user in database removing it from auth server");
          throw new UserRegistrationException("registration failed!!");
        }
    }
}
