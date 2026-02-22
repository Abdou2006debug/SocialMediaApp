package com.example.SocialMediaApp.Content.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommentResponse {
    private String id;
    private String content;
    private Instant createdAt;
    private long likeCount;
    private long replyCount;
}
