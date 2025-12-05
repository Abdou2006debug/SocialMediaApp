package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class user {
    private String useruuid;
    private String username;
    private String avatarurl;
    private String status;
    private String followuuid;
    public user(String uuid,String username,String avatarurl,String followuuid){
        this.useruuid=uuid;
        this.username=username;
        this.avatarurl=avatarurl;
        this.followuuid=followuuid;
    }
}
