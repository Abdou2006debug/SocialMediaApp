package com.example.whatsappclone.Shared.Mappers;

import com.example.whatsappclone.User.api.dto.userregistration;
import com.example.whatsappclone.User.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Usermapper {

    User toUserentity(userregistration userregistration);
}
