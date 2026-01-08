package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.Profile;
import com.example.whatsappclone.Configurations.Redis.RedisClasses.ProfileInfo;
import com.example.whatsappclone.Configurations.Redis.RedisClasses.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Cachemapper {
  User cacheUser(com.example.whatsappclone.Identity.domain.User user);
  Profile cacheProfile(com.example.whatsappclone.Profile.domain.Profile profile);
  com.example.whatsappclone.Identity.domain.User getUser(User user);
  com.example.whatsappclone.Profile.domain.Profile getProfile(Profile profile);
  @Mapping(source="publicavatarurl", target = "avatarurl")
  ProfileInfo cacheProfileInfo(com.example.whatsappclone.Profile.domain.Profile profile);
}
