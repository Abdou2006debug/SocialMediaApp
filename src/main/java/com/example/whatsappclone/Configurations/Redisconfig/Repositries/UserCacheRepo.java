package com.example.whatsappclone.Configurations.Redisconfig.Repositries;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserCacheRepo extends CrudRepository<User,String>{
}
