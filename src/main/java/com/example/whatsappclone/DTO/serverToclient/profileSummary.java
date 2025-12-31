package com.example.whatsappclone.DTO.serverToclient;

import com.example.whatsappclone.Entities.Follow;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class profileSummary {
    private String userId;
    private String username;
    private String avatarurl;
    private RelationshipStatus status;
    private String followId;
    public profileSummary(String userId){
        this.userId=userId;
    }
}
