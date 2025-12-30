package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.Profile;
import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Cachemapper {
  User cacheUser(com.example.whatsappclone.Entities.User user);
  Profile cacheProfile(com.example.whatsappclone.Entities.Profile profile);
  com.example.whatsappclone.Entities.User getUser(User user);
  com.example.whatsappclone.Entities.Profile getProfile(Profile profile);
  @Mapping(source="publicavatarurl", target = "pfpurl")
  ProfileInfo cacheProfileInfo(com.example.whatsappclone.Entities.Profile profile);
}
