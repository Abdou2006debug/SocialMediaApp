package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.Identity.api.dto.userregistration;
import com.example.whatsappclone.Identity.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Usermapper {

    User toUserentity(userregistration userregistration);
}
