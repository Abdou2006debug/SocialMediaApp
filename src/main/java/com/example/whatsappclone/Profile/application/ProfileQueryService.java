package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.DTO.serverToclient.profileDetails;
import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Identity.persistence.UserRepo;
import com.example.whatsappclone.Profile.domain.Profile;


import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Services.UserManagmentServices.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;;import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileQueryService {

    private final AuthenticatedUserService authenticatedUserService;
    private final CacheQueryService cacheQueryService;
    private final CacheWriterService cacheWriterService;
    private final ProfileRepo profileRepo;

    public profileDetails getMyProfile(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Profile profile=getuserprofile(currentuser,true);
        return null;
    }

    public com.example.whatsappclone.DTO.serverToclient.profilesettings getMyProfileSettings(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Profile profile=getuserprofile(currentuser,false);
        return new com.example.whatsappclone.DTO.serverToclient.
                profilesettings(profile.isIsprivate(),profile.isShowifonline());
    }

    public Profile getuserprofile(User user, Boolean cacheProfile){
        Optional<Profile> cached = cacheQueryService.getProfile(user.getUuid());
        Profile profile = cached.orElseGet(() -> profileRepo.findByUser(user).orElseThrow());
        if(cacheProfile &&cached.isEmpty()){
            cacheWriterService.cacheUserProfile(profile);
        }
        return profile;
    }
}
