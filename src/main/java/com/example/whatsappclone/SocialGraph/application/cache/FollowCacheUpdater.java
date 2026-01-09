package com.example.whatsappclone.SocialGraph.application.cache;

import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.domain.events.followRemoved;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowCacheUpdater {
    private final RedisTemplate<String,String> redisTemplate;
    private final FollowCacheWriter cacheWriterService;


    @EventListener
    public void addfollower(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followingId=follow.getFollowing().getUuid();

        if(redisTemplate.hasKey("user:"+followingId+":followers:page:"+0)){
            cacheWriterService.cacheUserFollowers(followingId,0);
        }
    }

    @EventListener
    public void removefollower(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();

        String page = redisTemplate.opsForValue().get("user:" + followingId + ":follower:" + followerId);
        if(page!=null){
            boolean areFollowersCached = redisTemplate.hasKey("user:"+followingId+":followers:page:"+Integer.parseInt(page));
            if(areFollowersCached){
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

        if(redisTemplate.hasKey("user:"+followerId+":following:page:"+0)){
            cacheWriterService.cacheUserFollowings(followerId,0);
        }
    }

    @EventListener
    public void removefollowing(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();

        String page = redisTemplate.opsForValue().get("user:" + followerId + ":following:" + followingId);
        if(page!=null){
            boolean areFollowingsCached= redisTemplate.hasKey("user:"+followerId+":followings:page:"+Integer.parseInt(page));
            if(areFollowingsCached){
                redisTemplate.delete("user:"+followerId+":followings:page:"+Integer.parseInt(page));
                cacheWriterService.cacheUserFollowings(followerId,Integer.parseInt(page));
            }
            redisTemplate.delete("user:"+followerId+":following:" +followingId);
        }
    }
}
