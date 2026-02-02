package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Messaging.api.dto.chatDTO;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Chatmapper {

    chatDTO tochatDTO(profileSummary profileSummary);
}
