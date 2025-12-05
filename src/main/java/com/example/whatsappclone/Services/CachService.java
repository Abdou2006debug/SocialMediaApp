package com.example.whatsappclone.Services;

import com.example.whatsappclone.Entities.Blocks;
import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.Profile;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Repositries.BlocksRepo;
import com.example.whatsappclone.Repositries.FollowRepo;
import com.example.whatsappclone.Repositries.ProfileRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
@Transactional
public class CachService {
    private final RedisTemplate<String,Object> redisTemplate;
    private final FollowRepo followRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;



    public void cachuser(User user){
        String keycloakid =user.getKeycloakId();
        redisTemplate.opsForHash().put("user:"+ keycloakid,"uuid",user.getUuid());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"username",user.getUsername());
        redisTemplate.opsForHash().put("user:"+keycloakid,"firstname",user.getFirstname());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"lastname",user.getLastname());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"email",user.getEmail());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"created date",user.getCreateddate().toString());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"lastmodifieddate",user.getLastmodifieddate().toString());
        redisTemplate.opsForHash().put("user:"+ keycloakid,"birthday",user.getBirthday());
        redisTemplate.opsForHash().put("user:"+user.getUuid(),"keycloak",keycloakid);
        redisTemplate.opsForValue().set("user:"+user.getUuid(),keycloakid);
        redisTemplate.expire("user:"+ keycloakid,5, TimeUnit.MINUTES);
        redisTemplate.expire("user:"+user.getUuid(),5,TimeUnit.MINUTES);
    }
    public void cachuserprofile(Profile profile){
        User user=profile.getUser();
        String keycloakid= user.getKeycloakId();
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"uuid",profile.getUuid());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"bio",profile.getBio());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"username",profile.getUsername());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"privateavatarurl",profile.getPrivateavatarurl());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"publicavatarurl",profile.getPublicavatarurl());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"showifonline",profile.isShowifonline());
        redisTemplate.opsForHash().put("user profile:"+ keycloakid,"isprivate",profile.isIsprivate());
        redisTemplate.expire("user profile:"+ keycloakid,2,TimeUnit.MINUTES);
    }
    public void cachuserfollowers(User user,int page) {
        Pageable pageable=PageRequest.of(page,10,Sort.by("accepteddate").descending());
        String keycloakId = user.getKeycloakId();
        Page<Follow> followerspage = followRepo.findByFollowingAndStatus(user, Follow.Status.ACCEPTED, pageable);
        List<Follow> followers=followerspage.getContent();
        followers.forEach(follow ->{
            String followerid=follow.getFollower().getKeycloakId();
            redisTemplate.opsForHash().put("user:"+keycloakId+":follower:"+followerid,"followid",follow.getUuid());
            redisTemplate.opsForHash().put("user:"+keycloakId+":follower:"+followerid,"page",page);
            redisTemplate.opsForZSet().add("user:"+keycloakId+":followers:page:"+page,followerid,follow.getAccepteddate().getEpochSecond());
            redisTemplate.expire("user:"+keycloakId+":followers:page:"+page,120,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+keycloakId+":follower:"+followerid,130,TimeUnit.SECONDS);
        });
    }

    public void cachuserfollowings(User user,int page) {
        Pageable pageable=PageRequest.of(page,10,Sort.by("accepteddate").descending());
        String keycloakId = user.getKeycloakId();
        Page<Follow> followingspage = followRepo.findByFollowerAndStatus(user, Follow.Status.ACCEPTED, pageable);
        List<Follow> followings=followingspage.getContent();
        followings.forEach(follow ->{
            String followingid=follow.getFollowing().getKeycloakId();
            redisTemplate.opsForHash().put("user:"+keycloakId+":following:"+followingid,"followid",follow.getUuid());
            redisTemplate.opsForHash().put("user:"+keycloakId+":following:"+followingid,"page",page);
            redisTemplate.opsForZSet().add("user:"+keycloakId+":followings:page:"+page,followingid,follow.getAccepteddate().getEpochSecond());
            redisTemplate.expire("user:"+keycloakId+":followings:page:"+page,120,TimeUnit.SECONDS);
            redisTemplate.expire("user:"+keycloakId+":following:"+followingid,130,TimeUnit.SECONDS);
        });
    }

    public void addfollower(User user, Follow follow) {
        String keycloakId = user.getKeycloakId();
        String followerId = follow.getFollower().getKeycloakId();
        String followid=follow.getUuid();
        boolean followerscached=redisTemplate.hasKey("user:"+keycloakId+":followers:page:"+0);
        if(!followerscached){
            cachuserfollowers(user,0);
        }
        redisTemplate.opsForHash().put("user:"+keycloakId+":follower:"+followerId,"followid",followid);
        redisTemplate.opsForHash().put("user:"+keycloakId+":follower:"+followerId,"page",0);
        redisTemplate.opsForZSet().add("user:"+keycloakId+":followers:page:" +0, followerId,follow.getAccepteddate().getEpochSecond());

        redisTemplate.expire("user:"+keycloakId+":followers:page:" +0,120,TimeUnit.SECONDS);

        redisTemplate.expire("user:"+keycloakId+":follower:"+followerId,130,TimeUnit.SECONDS);
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

    public void addfollowing(User user, Follow follow) {
        String keycloakId = user.getKeycloakId();
        String followingId = follow.getFollowing().getKeycloakId();
        String followid=follow.getUuid();
        boolean followingscached =redisTemplate.hasKey("user:"+keycloakId+":followings:page:"+0);
        if(!followingscached){
            cachuserfollowings(user,0);
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
        String keycloakid=user.getKeycloakId();
     Map<Object,Object> cachedprofile=redisTemplate.opsForHash().entries("user profile:"+keycloakid);
     if(cachedprofile.isEmpty()){
         return null;
     }
    Profile profile = new Profile();
    profile.setUuid((String) cachedprofile.get("uuid"));
    profile.setBio((String) cachedprofile.get("bio"));
    profile.setUsername((String) cachedprofile.get("username"));
    profile.setPrivateavatarurl((String) cachedprofile.get("privateavatarurl"));
    profile.setPublicavatarurl((String)cachedprofile.get("publicavatarurl"));
    profile.setShowifonline(Boolean.parseBoolean(cachedprofile.get("showifonline").toString()));
    profile.setIsprivate(Boolean.parseBoolean(cachedprofile.get("isprivate").toString()));
    profile.setUser(user);
return profile;
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

