package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Location;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Profile.api.dto.profileSummary;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostRepresentation {
    @JsonProperty("AuthorProfile")
    private profileSummary profileSummary;
    private String id;
    private String caption;
    private Post.PostStatus postStatus;
    private long likes;
    private long comments;
    private List<Media> mediaList;
    private Location location;
}
