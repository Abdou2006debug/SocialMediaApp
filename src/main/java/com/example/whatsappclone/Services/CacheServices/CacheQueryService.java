package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.UserCacheRepo;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheQueryService {
  private final UserCacheRepo userCacheRepo;
  private final ProfileCacheRepo profileCacheRepo;
  private final ProfileInfoCacheRepo profileInfoCacheRepo;
  private final Cachemapper mapper;
  public User getUser(String userId){
    return userCacheRepo.findById(userId).map(mapper::getUser).orElse(null);
  }
  public Profile getProfile(String userId){
     return profileCacheRepo.findByuserId(userId).map(profileCache -> {
       Profile profile=mapper.getProfile(profileCache);
        profile.setUser(new User(null,profileCache.getUserId()));
        return profile;
     }).orElse(null);
  }
  public ProfileInfo getProfileInfo(String userId){
return profileInfoCacheRepo.findById(userId).orElse(null);
  }
}
