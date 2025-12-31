package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.Configurations.Redisconfig.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Usermapper {
    profileSummary toSummary(ProfileInfo profileInfo);
}
