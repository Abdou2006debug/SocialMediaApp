package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redis.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redis.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redis.Repositries.UserCacheRepo;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CacheQueryService {
  private final UserCacheRepo userCacheRepo;
  private final ProfileCacheRepo profileCacheRepo;
  private final ProfileInfoCacheRepo profileInfoCacheRepo;
  private final Cachemapper mapper;
  private final RedisTemplate<String,String> redisTemplate;


  public Optional<User>  getUser(String userId){
    return userCacheRepo.findById(userId).map(mapper::getUser);
  }

  public Optional<Profile> getProfile(String userId){
     return profileCacheRepo.findByuserId(userId).map(profileCache -> {
       Profile profile=mapper.getProfile(profileCache);
        profile.setUser(new User(profileCache.getUserId()));
        return profile;
     });
  }

  public Optional<ProfileInfo>  getProfileInfo(String userId){
     return profileInfoCacheRepo.findById(userId);
  }

    public Optional<List<String>> getuserCachedFollowers(String userId , int page){
        boolean arefollowerscached = redisTemplate.hasKey("user:"+userId+":followers:page:"+page);
        if(arefollowerscached){
            Set<String> followersIds= redisTemplate.opsForZSet().reverseRange("user:"+userId+":followers:page:"+page,0,-1);
           List<String> followers=followersIds.stream().toList();
             return Optional.of(followers);
        }
         return Optional.empty();
    }

    public Optional<List<String>>  getusercachedfollowings(String userId,int page){
        boolean arefollowingscached = redisTemplate.hasKey("user:"+userId+":followings:page:"+page);
        if(arefollowingscached){
            Set<String> followingsIds = redisTemplate.opsForZSet().reverseRange("user:"+userId+":followings:page:"+page,0,-1);
            List<String> followings = followingsIds.stream().toList();
            return Optional.of(followings);
        }
        return Optional.empty();
    }

}
