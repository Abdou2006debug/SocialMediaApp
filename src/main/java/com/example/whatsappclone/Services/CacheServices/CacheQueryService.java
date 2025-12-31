package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.UserCacheRepo;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Optional<List<profileSummary>> getuserCachedFollowers(String userId , int page, boolean fetchfollowId){
        Pageable pageable= PageRequest.of(page,10, Sort.by("accepteddate").descending());
        boolean arefollowerscached = redisTemplate.hasKey("user:"+userId+":followers:page:"+page);
        if(arefollowerscached){
            Set<String> followersid= redisTemplate.opsForZSet().reverseRange("user:"+userId+":followers:page:"+page,0,-1);
           List<profileSummary> followers=followersid.stream().map(followerid->{
               profileSummary profileSummary=new profileSummary(followerid);
                if(fetchfollowId){
                    Map<Object,Object> followentries=  redisTemplate.opsForHash().entries("user:"+userId+":follower:"+followerid);
                    profileSummary.setFollowId((String) followentries.get("followId"));
                }
           return profileSummary;
            }).toList();
             return Optional.of(followers);
        }
         return Optional.empty();
    }

    public Optional<List<profileSummary>>  getusercachedfollowings(String userId,int page,boolean fetchfollowId){
        Pageable pageable= PageRequest.of(page,10, Sort.by("accepteddate").descending());
        boolean arefollowingscached = redisTemplate.hasKey("user:"+userId+":followings:page:"+page);
        if(arefollowingscached){
            Set<String> followersid= redisTemplate.opsForZSet().reverseRange("user:"+userId+":followings:page:"+page,0,-1);
            List<profileSummary> followings =followersid.stream().map(followerid->{
                profileSummary profileSummary=new profileSummary(followerid);
                if(fetchfollowId){
                    Map<Object,Object> followentries=  redisTemplate.opsForHash().entries("user:"+userId+":following:"+followerid);
                    profileSummary.setFollowId((String) followentries.get("followId"));
                }
                return profileSummary;
            }).toList();
            return Optional.of(followings);
        }
        return Optional.empty();
    }
}
