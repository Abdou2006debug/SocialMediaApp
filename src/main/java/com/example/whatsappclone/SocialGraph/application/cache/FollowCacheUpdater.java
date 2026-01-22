package com.example.whatsappclone.SocialGraph.application.cache;

import com.example.whatsappclone.SocialGraph.application.FollowQueryHelper;
import com.example.whatsappclone.SocialGraph.domain.Follow;
import com.example.whatsappclone.SocialGraph.domain.events.followAdded;
import com.example.whatsappclone.SocialGraph.domain.events.followRemoved;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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

        redisTemplate.opsForZSet().add("user:"+followingId+":followers",followerId,follow.getAccepteddate().getEpochSecond());
        redisTemplate.opsForZSet().add("user:"+followerId+":followings",followingId,follow.getAccepteddate().getEpochSecond());
    }

    @EventListener
    public void removefollow(followRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower().getUuid();
        String followingId =follow.getFollowing().getUuid();

        redisTemplate.opsForZSet().remove("user:"+followingId+":followers",followerId);
        redisTemplate.opsForZSet().remove("user:"+followerId+":followings",followingId);
        }

    public void UpdateCount(FollowQueryHelper.Position position,String userId,UpdateType updateType){
        int updateValue=updateType==UpdateType.INCREMENT?1:-1;
        // updating the follower and following count
        String key= FollowQueryHelper.Position.FOLLOWERS==position?"user:followers:":"user:followings:";
        if(redisTemplate.hasKey(key+userId)){
            redisTemplate.opsForValue().increment(key+userId,updateValue);
        }

    }
}
