package com.example.SocialMediaApp.Content.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private boolean liked;
    private long likeCount;
}
