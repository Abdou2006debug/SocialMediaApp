package com.example.whatsappclone.Services;

import com.example.whatsappclone.Configurations.Redisconfig.Cachemapper;
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
  public User getUser(String keycloakId){
     if(!userCacheRepo.existsBykeycloakId(keycloakId)){
         return null;
     }
      com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.User userCache=userCacheRepo.findBykeycloakId(keycloakId);
     return mapper.getUser(userCache);
  }
  public Profile getProfile(String keycloakId){
     if(!profileCacheRepo.existsBykeycloakId(keycloakId)){
         return null;
     }
     com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile profileCache=profileCacheRepo.findBykeycloakId(keycloakId);
     Profile profile=mapper.getProfile(profileCache);
     profile.setUser(new User(null,profileCache.getUserId()));
     return profile;
  }
  public ProfileInfo getProfileInfo(String userId){
return profileInfoCacheRepo.findById(userId).orElse(null);
  }
}
