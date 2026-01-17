package com.example.whatsappclone.Profile.api.dto;

import com.example.whatsappclone.SocialGraph.domain.RelationshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class profileDetails {
    private String userId;
    private String username;
    private String avatarurl;
    private String bio;
    private RelationshipStatus status;
    private long followers;
    private long followings;
    private String lastseen;
    private boolean isonline;
    public profileDetails(String userId,RelationshipStatus status){
        this.userId=userId;
        this.status=status;
    }
}
