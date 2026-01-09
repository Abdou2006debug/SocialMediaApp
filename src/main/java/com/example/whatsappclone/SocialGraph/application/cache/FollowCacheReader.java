package com.example.whatsappclone.SocialGraph.application.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowCacheReader {

    private final RedisTemplate<String,String> redisTemplate;

    public Optional<List<String>> getuserCachedFollowers(String userId , int page){
        boolean arefollowerscached = redisTemplate.hasKey("user:"+userId+":followers:page:"+page);
        if(arefollowerscached){
            Set<String> followersIds= redisTemplate.opsForZSet().reverseRange("user:"+userId+":followers:page:"+page,0,-1);
            List<String> followers=followersIds.stream().toList();
            return Optional.of(followers);
        }
        return Optional.empty();
    }

    public Optional<List<String>>  getusercachedfollowings(String userId,int page){
        boolean arefollowingscached = redisTemplate.hasKey("user:"+userId+":followings:page:"+page);
        if(arefollowingscached){
            Set<String> followingsIds = redisTemplate.opsForZSet().reverseRange("user:"+userId+":followings:page:"+page,0,-1);
            List<String> followings = followingsIds.stream().toList();
            return Optional.of(followings);
        }
        return Optional.empty();
    }

}
