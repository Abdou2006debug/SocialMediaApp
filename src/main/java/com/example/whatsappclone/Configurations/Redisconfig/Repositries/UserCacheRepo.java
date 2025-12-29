package com.example.whatsappclone.Configurations.Redisconfig.Repositries;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.User;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepo extends CrudRepository<User,String>{
     User findBykeycloakId(String keycloakId);
     boolean existsBykeycloakId(String keycloakId);
}
