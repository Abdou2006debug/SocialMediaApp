package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Entities.User;
import com.example.whatsappclone.Events.followAdded;
import com.example.whatsappclone.Events.followRemoved;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheUpdaterService {
    private final RedisTemplate<String,String> redisTemplate;
    @EventListener
    public void addfollower(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followingId=follow.getFollowing().getUuid();
        String followerId = follow.getFollower().getUuid();
        String followId=follow.getUuid();
        boolean areFollowersCached=redisTemplate.hasKey("user:"+followingId+":followers:page:"+0);
        if(areFollowersCached){
            redisTemplate.opsForHash().put("user:"+followingId+":follower:"+followerId,"followId",followId);
            redisTemplate.opsForHash().put("user:"+followingId+":follower:"+followerId,"page",0);
            redisTemplate.opsForZSet().add("user:"+followingId+":followers:page:" +0, followerId,follow.getAccepteddate().getEpochSecond());

            redisTemplate.expire("user:"+followingId+":follower:"+followerId,130,TimeUnit.SECONDS);
        }
    }
    @EventListener
    public void removefollower(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();
        String followId=follow.getUuid();
        boolean isfollowercached= redisTemplate.hasKey("user:"+followingId+":follower:"+followerId);
        if(isfollowercached){
            Map<Object,Object> r=redisTemplate.opsForHash().entries("user:"+followingId+":follower:"+followerId);
            int page = Integer.parseInt(r.get("page").toString());
            redisTemplate.opsForZSet().remove("user:"+followingId+":followers:page:" +page,followerId);
            redisTemplate.delete("user:"+followingId+":follower:" +followerId);
        }
    }
    @EventListener
    public void addfollowing(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followerId = follow.getFollower().getUuid();
        String followingId = follow.getFollowing().getUuid();
        String followid=follow.getUuid();
        boolean areFollowingsCached =redisTemplate.hasKey("user:"+followerId+":followings:page:"+0);
        if(areFollowingsCached){
            redisTemplate.opsForHash().put("user:"+followerId+":following:"+followingId,"followId",followid);
            redisTemplate.opsForHash().put("user:"+followerId+":following:"+followingId,"page",0);
            redisTemplate.opsForZSet().add("user:"+followerId+":followings:page:" +0, followingId,follow.getAccepteddate().getEpochSecond());
            redisTemplate.getExpire("user:")
            redisTemplate.expire("user:"+keycloakId+":followings:page:" +0,120,TimeUnit.SECONDS);
        }
    }
    @EventListener
    public void removefollowing(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId=follow.getFollowing().getUuid();
        String followId=follow.getUuid();
        boolean isfollowingcached= redisTemplate.hasKey("user:"+followerId+":following:"+followingId);
        if(isfollowingcached){
            Map<Object,Object> r=redisTemplate.opsForHash().entries("user:"+followerId+":following:"+followingId);
            int page = Integer.parseInt(r.get("page").toString());
            redisTemplate.opsForZSet().remove("user:"+followerId+":followings:page:" +page,followingId);
            redisTemplate.delete("user:"+followerId+":following:" +followingId);
        }
    }

}

