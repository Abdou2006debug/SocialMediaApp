package com.example.whatsappclone.Configurations.Redisconfig.Repositries;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileCacheRepo extends CrudRepository<Profile,String> {
    Optional<Profile> findByuserId(String userId);
}
