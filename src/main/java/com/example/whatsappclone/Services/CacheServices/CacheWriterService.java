package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.ProfileInfoCacheRepo;
import com.example.whatsappclone.Configurations.Redisconfig.Repositries.UserCacheRepo;
import com.example.whatsappclone.Entities.Blocks;
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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    profileInfoCache.setUuid(profile.getUser().getUuid());
    return profileInfoCacheRepo.save(profileInfoCache);
    }
    public void cacheUserProfile(Profile profile){
        com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile profileCache=mapper.cacheProfile(profile);
        profileCache.setUserId(profile.getUser().getUuid());
        profileCacheRepo.save(profileCache);
    }
    public void cachuserfollowers(String userId,int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followerspage = followRepo.findByFollowingUuidAndStatus(userId, Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        followers.forEach(follow ->{
            String followerid=follow.getFollower().getUuid();
            redisTemplate.opsForHash().put("user:"+userId+":follower:"+followerid,"followid",follow.getUuid());
            redisTemplate.opsForHash().put("user:"+userId+":follower:"+followerid,"page",page);
            redisTemplate.opsForZSet().add("user:"+userId+":followers:page:"+page,followerid,follow.getAccepteddate().getEpochSecond());
            redisTemplate.expire("user:"+userId+":followers:page:"+page,120,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+userId+":follower:"+followerid,130,TimeUnit.SECONDS);
        });
    }

    public void cachuserfollowings(String userId,int page) {
        Pageable pageable=PageRequest.of(page,20,Sort.by("accepteddate").descending());
        Page<Follow> followingspage = followRepo.findByFollowerUuidAndStatus(userId, Follow.Status.ACCEPTED, pageable);
        List<Follow> followings=followingspage.getContent();
        followings.forEach(follow ->{
            String followingid=follow.getFollowing().getUuid();
             redisTemplate.opsForHash().put("user:"+userId+":following:"+followingid,"followid",follow.getUuid());
             redisTemplate.opsForHash().put("user:"+userId+":following:"+followingid,"page",page);
            redisTemplate.opsForZSet().add("user:"+userId+":followings:page:"+page,followingid,follow.getAccepteddate().getEpochSecond());

            redisTemplate.expire("user:"+userId+":followings:page:"+page,120,TimeUnit.SECONDS);
            //redisTemplate.expire("user:"+keycloakId+":following:"+followingid,130,TimeUnit.SECONDS);
        });
    }

    public void addfollower(Follow follow) {
        String followingId=follow.getFollowing().getUuid();
        String followerId = follow.getFollower().getUuid();
        String followId=follow.getUuid();
        boolean followerscached=redisTemplate.hasKey("user:"+followingId+":followers:page:"+0);
        if(!followerscached){
           return;
        }
        redisTemplate.opsForHash().put("user:"+followingId+":follower:"+followerId,"followid",followId);
        redisTemplate.opsForHash().put("user:"+followingId+":follower:"+followerId,"page",0);
        redisTemplate.opsForZSet().add("user:"+followingId+":followers:page:" +0, followerId,follow.getAccepteddate().getEpochSecond());

        redisTemplate.expire("user:"+followingId+":followers:page:" +0,120,TimeUnit.SECONDS);
        redisTemplate.expire("user:"+followingId+":follower:"+followerId,130,TimeUnit.SECONDS);
    }

    public void removefollower(User user, Follow follow) {
        String keycloakId = user.getKeycloakId();
        String followerId = follow.getFollower().getKeycloakId();
        boolean isfollowercached= redisTemplate.hasKey("user:"+keycloakId+":follower:"+followerId);
        if(isfollowercached){
            Map<Object,Object> r=redisTemplate.opsForHash().entries("user:"+keycloakId+":follower:"+followerId);
            int page = Integer.parseInt(r.get("page").toString());
            redisTemplate.opsForZSet().remove("user:"+keycloakId+":followers:page:" +page,followerId);
            redisTemplate.delete("user:"+keycloakId+":follower:" +followerId);
        }
    }

    public void addfollowing(Follow follow) {
        String followerId = follow.getFollower().getUuid();
        String followingId = follow.getFollowing().getUuid();
        String followid=follow.getUuid();
        boolean followingscached =redisTemplate.hasKey("user:"+keycloakId+":followings:page:"+0);
        if(!followingscached){
           return;
        }
        redisTemplate.opsForHash().put("user:"+keycloakId+":following:"+followingId,"followid",followid);
        redisTemplate.opsForHash().put("user:"+keycloakId+":following:"+followingId,"page",0);
        redisTemplate.opsForZSet().add("user:"+keycloakId+":followings:page:" +0, followingId,follow.getAccepteddate().getEpochSecond());

        redisTemplate.expire("user:"+keycloakId+":followings:page:" +0,120,TimeUnit.SECONDS);

        redisTemplate.expire("user:"+keycloakId+":following:"+followingId,130,TimeUnit.SECONDS);
    }

    public void removefollowing(User user, Follow follow) {
        String keycloakId = user.getKeycloakId();
        String followingId = follow.getFollowing().getKeycloakId();
        boolean isfollowercached= redisTemplate.hasKey("user:"+keycloakId+":following:"+followingId);
        if(isfollowercached){
            Map<Object,Object> r=redisTemplate.opsForHash().entries("user:"+keycloakId+":following:"+followingId);
            int page = Integer.parseInt(r.get("page").toString());
            redisTemplate.opsForZSet().remove("user:"+keycloakId+":followings:page:" +page,followingId);
            redisTemplate.delete("user:"+keycloakId+":following:" +followingId);
        }
    }

    public  Map<String,String> getusercachedfollowers(User user,int page){
        Pageable pageable=PageRequest.of(page,10,Sort.by("accepteddate").descending());
        String keycloakid=user.getKeycloakId();
        boolean arefollowerscached = redisTemplate.hasKey("user:"+keycloakid+":followers:page:"+page);
        if(!arefollowerscached){return null;}
        Map<String,String> followersmap=new LinkedHashMap<>();
        List<String> followersid= redisTemplate.opsForZSet().reverseRange("user:"+keycloakid+":followers:page:"+page,0,-1).stream().map(f->(String)f).toList();
        followersid.forEach(followerid->{
            Map<Object,Object> r=  redisTemplate.opsForHash().entries("user:"+keycloakid+":follower:"+followerid);
            String followid=(String)r.get("followid");
            followersmap.put(followid,followerid);
        });
        return followersmap;
    }

    public  Map<String,String> getusercachedfollowings(User user,int page){
        Pageable pageable=PageRequest.of(page,10,Sort.by("accepteddate").descending());
        String keycloakid=user.getKeycloakId();
        boolean arefollowingscached = redisTemplate.hasKey("user:"+keycloakid+":followings:page:"+page);
        if(!arefollowingscached){return null;}
        Map<String,String> followingsmap=new LinkedHashMap<>();
        List<String> followersid= redisTemplate.opsForZSet().reverseRange("user:"+keycloakid+":followings:page:"+page,0,-1).stream().map(f->(String)f).toList();
        followersid.forEach(followerid->{
            Map<Object,Object> r=  redisTemplate.opsForHash().entries("user:"+keycloakid+":following:"+followerid);
            String followid=(String)r.get("followid");
            followingsmap.put(followid,followerid);
        });
        return followingsmap;
    }
public Profile getcachedprofile(User user){
   return null;
}

public User getUserbyId(String userid){
    String keycloakid=(String)redisTemplate.opsForValue().get("user:"+userid);
        if(keycloakid==null){
            return null;
        }
        return  getUserbyKeycloakId(keycloakid);
    }
    public User getUserbyKeycloakId(String keyclaokid){
       Map<Object,Object> entries=redisTemplate.opsForHash().entries("user:"+keyclaokid);
       if(entries.isEmpty()){
          return null;
   }
       User user =new User((String)entries
               .get("username"),(String)entries.get("firstname"),(String)entries.get("lastname"),(String) entries.get("email"),keyclaokid);
       user.setUuid((String) entries.get("uuid"));
       return user;
    }
   public void cachblockedusers(User user){
        String keycloakid=user.getKeycloakId();
      List<Blocks> usersblocked= blocksRepo.findByBlocker(user);
      List<String> usersblockedid=usersblocked.stream()
              .map(blocks -> blocks.getBlocked().getKeycloakId()).toList();
      redisTemplate.opsForSet().add("user blocked:"+keycloakid,usersblockedid.toArray(new String[0]));
      redisTemplate.expire("user blocked:"+keycloakid,2,TimeUnit.MINUTES);
    }
public void setuserstatus(boolean status,String username){
        if(!status){
            redisTemplate.delete("user: "+username);
            return;
        }
        redisTemplate.opsForValue().set("user: "+username,"online");
        redisTemplate.expire("user: "+username,30,TimeUnit.SECONDS);
}
public void setuserlastseen(String username){
        redisTemplate.opsForValue().set("user lastseen:"+username,Instant.now().toString());
}
public String getuserlastseen(String username){
        return (String)redisTemplate.opsForValue().get("user lastseen:"+username);
}
public boolean getuserstatus(String username){
    return redisTemplate.hasKey("user: "+username);
}
}

