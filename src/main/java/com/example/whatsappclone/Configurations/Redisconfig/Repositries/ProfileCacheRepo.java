package com.example.whatsappclone.Configurations.Redisconfig.Repositries;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile;
import org.springframework.data.repository.CrudRepository;

public interface ProfileCacheRepo extends CrudRepository<Profile,String> {
 Profile findBykeycloakId(String keycloakId);
    boolean existsBykeycloakId(String keycloakId);
}
