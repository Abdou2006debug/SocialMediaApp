package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Content.api.dto.postCreation;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Contentmapper {
    PostSettings toPostSettings(postCreation postCreation);
}

