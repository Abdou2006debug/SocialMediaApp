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

    public void setuserstatus(boolean status,String username){
        if(!status){
            redisTemplate.delete("user: "+username);
            return;
        }
        redisTemplate.opsForValue().set("user: "+username,"online");
        redisTemplate.expire("user: "+username,30, TimeUnit.SECONDS);
    }

    public void setUserLastSeen(String username){
        redisTemplate.opsForValue().set("user lastseen:"+username, Instant.now().toString());
    }

    public String getUserLastSeen(String username){
        return (String)redisTemplate.opsForValue().get("user lastseen:"+username);
    }

    public boolean getUserStatus(String username){
        return redisTemplate.hasKey("user: "+username);
    }
}
