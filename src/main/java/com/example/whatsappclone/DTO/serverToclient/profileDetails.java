package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class profileDetails {
    private String userId;
    private String username;
    private String avatarurl;
    private String bio;
    private RelationshipStatus status;
    private String followId;
    private long followers;
    private long followings;
    private String lastseen;
    private boolean isonline;
}
