package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Entities.Follow;
import com.example.whatsappclone.Events.followAdded;
import com.example.whatsappclone.Events.followRemoved;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheUpdaterService {
    private final RedisTemplate<String,String> redisTemplate;
    private final CacheWriterService cacheWriterService;

    @EventListener
    public void addfollower(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followingId=follow.getFollowing().getUuid();
        String followerId = follow.getFollower().getUuid();
        boolean areFollowersCached=redisTemplate.hasKey("user:"+followingId+":followers:page:"+0);
        if(areFollowersCached){

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

        String page = redisTemplate.opsForValue().get("user:" + followingId + ":follower:" + followerId);
        if(page!=null){
            boolean areFollowingsCached= redisTemplate.hasKey("user:"+followingId+":followers:page:"+Integer.parseInt(page));
            if(areFollowingsCached){
                redisTemplate.delete("user:"+followingId+":followers:page:"+Integer.parseInt(page));
                cacheWriterService.cacheUserFollowers(followingId,Integer.parseInt(page));
            }
            redisTemplate.delete("user:"+followingId+":follower:" +followerId);
        }
    }
    @EventListener
    public void addfollowing(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followerId = follow.getFollower().getUuid();
        String followingId = follow.getFollowing().getUuid();
        boolean areFollowingsCached =redisTemplate.hasKey("user:"+followerId+":followings:page:"+0);
        if(areFollowingsCached){
            redisTemplate.opsForHash().put("user:"+followerId+":following:"+followingId,"page",0);
            redisTemplate.opsForZSet().add("user:"+followerId+":followings:page:" +0, followingId,follow.getAccepteddate().getEpochSecond());

            redisTemplate.expire("user:"+followerId+":followings:page:" +0,120,TimeUnit.SECONDS);
        }
    }
    @EventListener
    public void removefollowing(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId=follow.getFollowing().getUuid();
        String page=redisTemplate.opsForValue().get("user:"+followerId+":following:"+followingId);
        if(page!=null){
            boolean areFollowingCached=redisTemplate.hasKey("user:"+followerId+":followings:page:"+Integer.parseInt(page));
            if(areFollowingCached){
                redisTemplate.delete("user:"+followerId+":followings:page:" +Integer.parseInt(page));
                cacheWriterService.cacheUserFollowings(followerId,Integer.parseInt(page));
            }
            redisTemplate.delete("user:"+followerId+":following:" +followingId);
        }
    }

}

