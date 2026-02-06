package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Notification.api.dto.notificationsettings;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Profile.api.dto.profileDetails;
import com.example.SocialMediaApp.Profile.api.dto.profilesettings;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Profilemapper {
   profileDetails toprofileDetails(ProfileInfo profileInfo);

   // profileSummary toprofileSummary(ProfileInfo profileInfo);


    Profile toprofile(com.example.SocialMediaApp.Profile.domain.cache.Profile profile);

    com.example.SocialMediaApp.Profile.domain.cache.Profile toprofileCache(Profile profile);

    profilesettings toprofilesettings(Profile profile);

    @Mapping(target = "avatarurl", source = "publicavatarurl")
    ProfileInfo toprofileInfo(Profile profile);
    notificationsettings tonotificationsettings(NotificationsSettings notificationsSettings);
}
