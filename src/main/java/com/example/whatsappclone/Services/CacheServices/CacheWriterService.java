package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.UserCacheRepo;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class CacheWriterService {
    private final RedisTemplate<String,Object> redisTemplate;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;
    private final UserCacheRepo usercacheRepo;
    private final ProfileCacheRepo profileCacheRepo;
    private final ProfileInfoCacheRepo profileInfoCacheRepo;
    private final Cachemapper mapper;

    public void cacheUser(User user){
    com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.User cachedUser=mapper.cacheUser(user);
    usercacheRepo.save(cachedUser);
    }

    public ProfileInfo cacheProfileInfo(Profile profile){
    ProfileInfo profileInfoCache=mapper.cacheProfileInfo(profile);
    profileInfoCache.setUserId(profile.getUserId());
    return profileInfoCacheRepo.save(profileInfoCache);
    }

    public void cacheUserProfile(Profile profile){
        com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile profileCache=mapper.cacheProfile(profile);
        profileCache.setUserId(profile.getUserId());
        profileCacheRepo.save(profileCache);
    }

    public List<String> cacheUserFollowers(String userId,int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followerspage = followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        List<String> followersIds=new ArrayList<>();
        followers.forEach(follow ->{
            String followerId=follow.getFollower_id();
            followersIds.add(followerId);
            // used when updating the follow to know exactly which page to update
            redisTemplate.opsForValue().set("user:"+userId+":follower:"+followerId,page);
            redisTemplate.opsForZSet().add("user:"+userId+":followers:page:"+page,followerId,follow.getAccepteddate().getEpochSecond());
            long ttl=page==0?10:5;
            redisTemplate.expire("user:"+userId+":follower:page:"+page,ttl,TimeUnit.MINUTES);
            redisTemplate.expire("user:"+userId+":follower:"+followerId,ttl+2,TimeUnit.MINUTES);
        });
        return followersIds;
    }

    public List<String> cacheUserFollowings(String userId, int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followingspage = followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followings=followingspage.getContent();
        List<String> followingsIds =new ArrayList<>();
        followings.forEach(follow ->{
            String followingId=follow.getFollowing_id();
            followingsIds.add(followingId);
            redisTemplate.opsForValue().set("user:"+userId+":following:"+followingId,page);
            redisTemplate.opsForZSet().add("user:"+userId+":followings:page:"+page,followingId,follow.getAccepteddate().getEpochSecond());
            long ttl=page==0?10:5;
            redisTemplate.expire("user:"+userId+":followings:page:"+page,ttl,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+userId+":following:"+followingId,ttl+2,TimeUnit.SECONDS);
        });
        return followingsIds;
    }

}

