package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class profile{
    private String useruuid;
private String username;
    private String bio;
    private String avatarurl;
   private long followers;
   private long followings;
}
