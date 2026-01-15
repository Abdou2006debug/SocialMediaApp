package com.example.whatsappclone.User.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserActivityService {
    private final RedisTemplate<String,String> redisTemplate;

    public void setuserstatus(boolean status,String userId){
        if(!status){
            redisTemplate.delete("user: "+userId);
            return;
        }
        redisTemplate.opsForValue().set("user: "+userId,"online");
        redisTemplate.expire("user: "+userId,30, TimeUnit.SECONDS);
    }

    public void setUserLastSeen(String username){
        redisTemplate.opsForValue().set("user lastSeen:"+username, Instant.now().toString());
    }

    public String getUserLastSeen(String username){
        return (String) redisTemplate.opsForValue().get("user lastSeen:"+username);
    }

    public boolean getUserStatus(String userId){
        return redisTemplate.hasKey("user: "+ userId);
    }
}
