package com.example.whatsappclone.Services.UserManagmentServices;

import com.example.whatsappclone.Configurations.Redis.RedisClasses.ProfileInfo;
import com.example.whatsappclone.DTO.serverToclient.profileDetails;
import com.example.whatsappclone.DTO.serverToclient.profileSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Usermapper {
    profileDetails toprofileDetails(ProfileInfo profileInfo);
    profileSummary toprofileSummary(ProfileInfo profileInfo);
}
