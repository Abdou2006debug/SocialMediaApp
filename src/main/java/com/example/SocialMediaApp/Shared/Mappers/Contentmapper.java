package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.domain.*;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Contentmapper {

    PostSettings toPostSettings(PostCreationRequest postCreation);
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "postStatus" ,ignore = true)
    PostRepresentation toPostRepresentation(Post post);
    CommentResponse toCommentResponse(Comment comment);
    MediaRepresentation toMediaRepresentation(Media media);
    @Mapping(target = "storyStatus",ignore = true)
    StoryRepresentation toStoryRepresentation(Story story);
}

