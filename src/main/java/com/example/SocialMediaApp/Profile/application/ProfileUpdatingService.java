package com.example.SocialMediaApp.Profile.application;

import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.application.IdentityService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import com.example.SocialMediaApp.Profile.api.dto.profile;
import com.example.SocialMediaApp.Profile.api.dto.profilesettings;
import com.example.SocialMediaApp.Profile.application.cache.ProfileCacheManager;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        String currentUserId= authenticatedUserService.getcurrentuser();
        Profile currentprofile= profileQueryService.getUserProfile(currentUserId,false);
        boolean preStatus=currentprofile.isIsprivate();
        currentprofile.setIsprivate(profilesettings.isIsprivate());
        currentprofile.setShowifonline(profilesettings.isShowifonline());
        profileRepo.save(currentprofile);
        profileCacheManager.cacheUserProfile(currentprofile);
        // if profile was set from private to public all follow request to this user must be deleted
        if(preStatus&&!profilesettings.isIsprivate()){
            followRepo.deleteByFollowingIdAndStatus(currentUserId, Follow.Status.PENDING);
        }
    }

    public void changeProfileAvatar(MultipartFile file) throws IOException {
        String currentUserId= authenticatedUserService.getcurrentuser();

        Profile currentprofile= profileQueryService.getUserProfile(currentUserId,false);

        String oldAvatarUri=currentprofile.getPrivateavatarurl();

        String profileAvatarUri=storageService.uploadFile(file,currentUserId);

        if(oldAvatarUri!=null){
                storageService.deleteFile(oldAvatarUri);
        }

        currentprofile.setPrivateavatarurl(profileAvatarUri.replace("/public",""));
        currentprofile.setPublicavatarurl(profileAvatarUri);
        profileRepo.save(currentprofile);
        // in case of cache still valid to update it to avoid any inconsistencies
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }

    @Transactional
    public void UpdateProfile(profile profile){
        String currentUserId=authenticatedUserService.getcurrentuser();
        User  currentuser= userRepo.findById(currentUserId).get();
        Profile currentprofile= profileQueryService.getUserProfile(currentuser.getId(),false);
        currentprofile.setUsername(profile.getUsername());
        currentprofile.setBio(profile.getBio());
        currentuser.setUsername(profile.getUsername());
        currentprofile.setUsername(profile.getUsername());
        currentuser.setProfile(currentprofile);
        userRepo.save(currentuser);
        if(!currentprofile.getUsername().equals(profile.getUsername())){
            identityService.changeUsername(currentUserId, profile.getUsername());
        }
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }
}
