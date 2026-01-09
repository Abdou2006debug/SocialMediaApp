package com.example.whatsappclone.Profile.api.dto;

import com.example.whatsappclone.DTO.serverToclient.RelationshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class profileSummary {
    private String userId;
    private String username;
    private String avatarurl;
    private RelationshipStatus status;
    public profileSummary(String userId){
        this.userId=userId;
    }
}
