package com.example.whatsappclone.DTO.serverToclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class notification {
    private String message;
    private String pfpurl;
    private String userid;
    private String followid;
    public notification(String message,String pfpurl,String userid,String followid){
        this.message=message;
        this.pfpurl=pfpurl;
        this.userid=userid;
        this.followid=followid;
    }
}
