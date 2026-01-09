package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.Identity.application.AuthenticatedUserService;
import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;


import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Services.CacheServices.CacheQueryService;
import com.example.whatsappclone.Services.CacheServices.CacheWriterService;
import com.example.whatsappclone.Shared.Mappers.Profilemapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;;import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileQueryService {

    private final AuthenticatedUserService authenticatedUserService;
    private final CacheQueryService cacheQueryService;
    private final ProfileCacheManager profileCacheManager;
    private final ProfileRepo profileRepo;
    private final Profilemapper profilemapper;

    public profileDetails getMyProfile(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Profile profile=getuserprofile(currentuser,true);
        return null;
    }

    public profilesettings getMyProfileSettings(){
        User currentuser=authenticatedUserService.getcurrentuser(false);
        Profile profile=getuserprofile(currentuser,true);
        return profilemapper.toprofilesettings(profile);
    }

    public Profile getuserprofile(User user, Boolean cacheProfile){
        Optional<Profile> cached = cacheQueryService.getProfile(user.getUuid());
        Profile profile = cached.orElseGet(() -> profileRepo.findByUser(user).orElseThrow());
        if(cacheProfile &&cached.isEmpty()){
            profileCacheManager.cacheUserProfile(profile);
        }
        return profile;
    }
}
