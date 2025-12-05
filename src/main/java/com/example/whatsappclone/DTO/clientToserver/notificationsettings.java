package com.example.whatsappclone.DTO.clientToserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class notificationsettings {
    private boolean onfollowingrequests_accepted;
    private boolean onfollowingrequests_rejected;
    private boolean onfollower;
}
