package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    public profileDetails(String uuid, String username, String avatarurl, String bio,
                          RelationshipStatus status, long followers, long followings, String lastseen, boolean isonline){
        this.username=username;
        this.avatarurl=avatarurl;
        this.lastseen=lastseen;
        this.bio=bio;
        this.status=status;
        this.isonline=isonline;
        this.followers=followers;
        this.followings=followings;
    }
}
