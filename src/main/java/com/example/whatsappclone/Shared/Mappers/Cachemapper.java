package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Identity.domain.cache.User;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Cachemapper {
  User cacheUser(com.example.whatsappclone.Identity.domain.User user);
  com.example.whatsappclone.Profile.domain.cache.Profile cacheProfile(com.example.whatsappclone.Profile.domain.Profile profile);
  com.example.whatsappclone.Identity.domain.User getUser(User user);
  com.example.whatsappclone.Profile.domain.Profile getProfile(com.example.whatsappclone.Profile.domain.cache.Profile profile);
  @Mapping(source="publicavatarurl", target = "avatarurl")
  ProfileInfo cacheProfileInfo(com.example.whatsappclone.Profile.domain.Profile profile);
}
