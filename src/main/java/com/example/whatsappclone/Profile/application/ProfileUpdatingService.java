package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.persistence.FollowRepo;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.application.IdentityService;
import com.example.whatsappclone.User.application.RegistrationService;
import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.User.persistence.UserRepo;
import com.example.whatsappclone.Profile.api.dto.profile;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileUpdatingService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileRepo profileRepo;
    private final ProfileCacheManager profileCacheManager;
    private final UserRepo userRepo;
    private final ProfileQueryService profileQueryService;
    private final StorageService storageService;
    private final FollowRepo followRepo;
    private final IdentityService identityService;

    public void UpdateProfileSettings(profilesettings profilesettings){
        User currentuser= authenticatedUserService.getcurrentuser();
        Profile currentprofile= profileQueryService.getUserProfile(currentuser.getId(),false);
        boolean preStatus=currentprofile.isIsprivate();
        currentprofile.setIsprivate(profilesettings.isIsprivate());
        currentprofile.setShowifonline(profilesettings.isShowifonline());
        profileRepo.save(currentprofile);
        profileCacheManager.cacheUserProfile(currentprofile);
        // if profile was set from private to public all follow request to this user must be deleted
        if(preStatus&&!profilesettings.isIsprivate()){
            followRepo.deleteByFollowingAndStatus(currentuser, Follow.Status.PENDING);
        }
    }

    public void changeProfileAvatar(MultipartFile file) throws IOException {
        User currentuser= authenticatedUserService.getcurrentuser();
        Profile currentprofile= profileQueryService.getUserProfile(currentuser.getId(),false);

        String oldAvatarUri=currentprofile.getPrivateavatarurl();

      String profileAvatarUri=storageService.uploadAvatartoStorage(file,oldAvatarUri);
        currentprofile.setPrivateavatarurl(profileAvatarUri.replace("/public",""));
        currentprofile.setPublicavatarurl(profileAvatarUri);
        profileRepo.save(currentprofile);
        // in case of cache still valid to update it to avoid any inconsistencies
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }
    @Transactional
    public void UpdateProfile(profile p){
        User  currentuser= userRepo.findById(authenticatedUserService.getcurrentuser().getId()).get();
        Profile currentprofile= profileQueryService.getUserProfile(currentuser.getId(),false);
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        currentprofile.setUsername(p.getUsername());
        userRepo.save(currentuser);
        profileRepo.save(currentprofile);
        identityService.changeUsername(currentuser.getId(),p.getUsername());
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }
}
