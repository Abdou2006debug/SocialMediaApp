package com.example.whatsappclone.DTO.clientToserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class profilesettings {
    private boolean isprivate;
    private boolean showifonline;
    public profilesettings(boolean isprivate){
        this.isprivate=isprivate;
    }
}
