package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Content.api.dto.CommentResponse;
import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.Comment;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
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
}

