package com.example.whatsappclone.Configurations.Redis.Repositries;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.User;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepo extends CrudRepository<User,String>{
}
