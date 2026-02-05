package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Messaging.api.dto.chatSummary;
import com.example.whatsappclone.Messaging.api.dto.chatUser;
import com.example.whatsappclone.Messaging.api.dto.messageDTO;
import com.example.whatsappclone.Messaging.domain.Message;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.domain.Profile;
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
