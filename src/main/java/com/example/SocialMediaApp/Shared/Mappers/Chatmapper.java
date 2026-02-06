package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Messaging.api.dto.chatSummary;
import com.example.SocialMediaApp.Messaging.api.dto.chatUser;
import com.example.SocialMediaApp.Messaging.api.dto.messageDTO;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Profile.api.dto.profileSummary;
import com.example.SocialMediaApp.Profile.domain.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Chatmapper {



    @Mapping(target = "avatarurl", source = "publicavatarurl")
    chatUser tochatUser(Profile profile);
    @Mapping(target = "messageId", source = "id")
    messageDTO tomessageDTO(Message message);
    chatSummary tochatDTO(profileSummary profileSummary);
}
