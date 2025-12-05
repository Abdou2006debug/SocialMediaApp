package com.example.whatsappclone.DTO.serverToclient;

import lombok.Data;

@Data
public class profilesettings {
    private boolean isprivate;
    private boolean showifonline;
    public profilesettings(boolean isprivate,boolean showifonline){
        this.isprivate=isprivate;
        this.showifonline=showifonline;
    }
}
