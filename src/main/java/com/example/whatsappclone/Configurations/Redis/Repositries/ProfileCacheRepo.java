package com.example.whatsappclone.Configurations.Redis.Repositries;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileCacheRepo extends CrudRepository<Profile,String> {
    Optional<Profile> findByuserId(String userId);
}
