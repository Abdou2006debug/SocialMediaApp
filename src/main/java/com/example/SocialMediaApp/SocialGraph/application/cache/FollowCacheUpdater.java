package com.example.SocialMediaApp.SocialGraph.application.cache;

import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.events.followAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.followRemoved;
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
    public enum UpdateType{INCREMENT,DECREMENT}

    @EventListener
    public void addfollow(followAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();
        boolean followersPage0Cached = redisTemplate.hasKey("user:" + followingId + ":followers:page0_cached");
        boolean followingsPage0Cached = redisTemplate.hasKey("user:" + followerId + ":followings:page0_cached");

        if (followersPage0Cached) {
            redisTemplate.opsForZSet().add(
                    "user:" + followingId + ":followers:",
                    followerId,
                    follow.getFollowDate().getEpochSecond()
            );
        }

        if (followingsPage0Cached) {
            redisTemplate.opsForZSet().add(
                    "user:" + followerId + ":followings:",
                    followingId,
                    follow.getFollowDate().getEpochSecond()
            );
        }


    }

    @EventListener
    public void removefollow(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();
        redisTemplate.opsForZSet().remove("user:"+followingId+":followers:",followerId);
        redisTemplate.opsForZSet().remove("user:"+followerId+":followings:",followingId);
        }

    public void UpdateCount(FollowQueryHelper.Position position,String userId,UpdateType updateType){
        int updateValue=updateType==UpdateType.INCREMENT?1:-1;
        // updating the follower and following count
        String key= FollowQueryHelper.Position.FOLLOWERS==position?"user:followers:":"user:followings:";
        if(redisTemplate.hasKey(key+userId)){
           Long count= redisTemplate.opsForValue().increment(key+userId,updateValue);
           log.info("new "+position.name()+" count "+count);
        }

    }
}
