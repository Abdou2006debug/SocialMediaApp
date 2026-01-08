package com.example.whatsappclone.Services.CacheServices;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.Profile;
import com.example.whatsappclone.Configurations.Redis.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redis.RedisClasses.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Cachemapper {
  User cacheUser(com.example.whatsappclone.Entities.User user);
  Profile cacheProfile(com.example.whatsappclone.Entities.Profile profile);
  com.example.whatsappclone.Entities.User getUser(User user);
  com.example.whatsappclone.Entities.Profile getProfile(Profile profile);
  @Mapping(source="publicavatarurl", target = "avatarurl")
  ProfileInfo cacheProfileInfo(com.example.whatsappclone.Entities.Profile profile);
}
