package com.example.SocialMediaApp.Content.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
public class LikeResponse {
    private boolean liked;
    private long likeCount;

    public LikeResponse(boolean liked){
        this.liked=liked;
    }

}
