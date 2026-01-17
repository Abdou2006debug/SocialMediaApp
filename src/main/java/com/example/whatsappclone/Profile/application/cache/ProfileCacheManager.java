package com.example.whatsappclone.Profile.application.cache;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileCacheRepo;
import com.example.whatsappclone.Profile.persistence.ProfileInfoCacheRepo;
import com.example.whatsappclone.Shared.Mappers.Profilemapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileCacheManager {

    private final ProfileCacheRepo profileCacheRepo;
    private final Profilemapper profilemapper;
    private final ProfileInfoCacheRepo profileInfoCacheRepo;


    public ProfileInfo cacheProfileInfo(Profile profile){
        ProfileInfo profileInfoCache=profilemapper.toprofileInfo(profile);
        profileInfoCache.setUserId(profile.getUserId());
        return profileInfoCacheRepo.save(profileInfoCache);
    }

    public void cacheUserProfile(Profile profile){
        com.example.whatsappclone.Profile.domain.cache.Profile profileCache=profilemapper.toprofileCache(profile);
        profileCache.setUserId(profile.getUserId());
        profileCacheRepo.save(profileCache);
    }

    public Optional<Profile> getProfile(String userId){
        return profileCacheRepo.findByUserId(userId).map(profileCache -> {
            Profile profile=profilemapper.toprofile(profileCache);
            profile.setUser(new User(profileCache.getUserId()));
            return profile;
        });
    }

    public Optional<ProfileInfo>  getProfileInfo(String userId){
        return profileInfoCacheRepo.findById(userId);
    }

}
