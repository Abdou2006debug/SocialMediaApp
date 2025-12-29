package com.example.whatsappclone.Configurations.Redisconfig.Repositries;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import org.springframework.data.repository.CrudRepository;

public interface ProfileInfoCacheRepo extends CrudRepository<ProfileInfo,String> {
    ProfileInfo findByuserId(String userId);
    boolean existsByuserId(String userId);
}
