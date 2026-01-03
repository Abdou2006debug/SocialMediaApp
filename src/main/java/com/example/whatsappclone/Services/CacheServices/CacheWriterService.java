package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.UserCacheRepo;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
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

    public List<profileSummary> cachUserFollowers(String userId,int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followerspage = followRepo.findByFollowingAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        followers.forEach(follow ->{
            String followerid=follow.getFollower_id();
            redisTemplate.opsForHash().put("user:"+userId+":follower:"+followerid,"followId",follow.getUuid());
            redisTemplate.opsForHash().put("user:"+userId+":follower:"+followerid,"page",page);
            redisTemplate.opsForZSet().add("user:"+userId+":followers:page:"+page,followerid,follow.getAccepteddate().getEpochSecond());
            redisTemplate.expire("user:"+userId+":followers:page:"+page,120,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+userId+":follower:"+followerid,130,TimeUnit.SECONDS);
        });
        return null;
    }

    public List<profileSummary> cachUserFollowings(String userId, int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followingspage = followRepo.findByFollowerAndStatus(new User(userId), Follow.Status.ACCEPTED, pageable);
        List<Follow> followings=followingspage.getContent();
        followings.forEach(follow ->{
            String followingid=follow.getFollowing_id();
             redisTemplate.opsForHash().put("user:"+userId+":following:"+followingid,"followId",follow.getUuid());
             redisTemplate.opsForHash().put("user:"+userId+":following:"+followingid,"page",page);
            redisTemplate.opsForZSet().add("user:"+userId+":followings:page:"+page,followingid,follow.getAccepteddate().getEpochSecond());

            redisTemplate.expire("user:"+userId+":followings:page:"+page,120,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+userId+":following:"+followingid,130,TimeUnit.SECONDS);
        });
        return null;
    }
   //public void cachblockedusers(User user){
     //   String keycloakid=user.getKeycloakId();
     // List<Blocks> usersblocked= blocksRepo.findByBlocker(user);
     // List<String> usersblockedid=usersblocked.stream()
      //        .map(blocks -> blocks.getBlocked().getKeycloakId()).toList();
     // redisTemplate.opsForSet().add("user blocked:"+keycloakid,usersblockedid.toArray(new String[0]));
     // redisTemplate.expire("user blocked:"+keycloakid,2,TimeUnit.MINUTES);
    //}
}

