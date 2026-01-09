package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Profile.api.dto.profileDetails;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.api.dto.profilesettings;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Profilemapper {
   profileDetails toprofileDetails(ProfileInfo profileInfo);

   // profileSummary toprofileSummary(ProfileInfo profileInfo);


    Profile toprofile(com.example.whatsappclone.Profile.domain.cache.Profile profile);

    com.example.whatsappclone.Profile.domain.cache.Profile toprofileCache(Profile profile);

    profilesettings toprofilesettings(Profile profile);

    @Mapping(target = "avatarurl", source = "publicavatarurl")
    ProfileInfo toprofileInfo(Profile profile);
}
