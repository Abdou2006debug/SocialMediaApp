package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Profile.api.dto.profile;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Storage.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "storage.bucket")
public class ProfileUpdatingService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileRepo profileRepo;
    private final ProfileCacheManager profileCacheManager;
    private final UserRepo userRepo;
    private final ProfileQueryService profileQueryService;
    private final StorageService storageService;


    public void UpdateProfileSettings(profilesettings profilesettings){
        User currentuser= authenticatedUserService.getcurrentuser(false);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);
        currentprofile.setIsprivate(profilesettings.isIsprivate());
        currentprofile.setShowifonline(profilesettings.isShowifonline());
        profileRepo.save(currentprofile);
        profileCacheManager.cacheUserProfile(currentprofile);
    }

    public void changeProfileAvatar(MultipartFile file) throws IOException {
        User currentuser= authenticatedUserService.getcurrentuser(false);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);

        String oldAvatarUri=currentprofile.getPrivateavatarurl();

        String profileAvatarUri=storageService.uploadAvatartoStorage(file,oldAvatarUri);
        currentprofile.setPrivateavatarurl(profileAvatarUri.replace("/public",""));
        currentprofile.setPublicavatarurl(profileAvatarUri);
        profileRepo.save(currentprofile);
        // in case of cache still valid to update it to avoid any inconsistencies
        profileCacheManager.cacheProfileInfo(currentprofile);
    }

    public void UpdateProfile(profile p){
        User currentuser= authenticatedUserService.getcurrentuser(true);
        Profile currentprofile= profileQueryService.getuserprofile(currentuser,false);
        currentprofile.setUsername(p.getUsername());
        currentprofile.setBio(p.getBio());
        currentuser.setUsername(p.getUsername());
        currentprofile.setUsername(p.getUsername());
        userRepo.save(currentuser);
        profileRepo.save(currentprofile);
        profileCacheManager.cacheUserProfile(currentprofile);
    }
}
