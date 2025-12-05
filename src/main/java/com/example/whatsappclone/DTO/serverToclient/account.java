package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class account {
    private String uuid;
    private String username;
    private String avatarurl;
    private String bio;
    private String status;
    private long followers;
    private long followings;
    private String lastseen;
    private boolean isonline;
    public account(String uuid,String username,String avatarurl,String bio,String status,long followers,long followings,String lastseen,boolean isonline){
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
