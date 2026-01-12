package com.example.whatsappclone.Notification.api;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealtimeController {
    private final CacheWriterService cachservice;
    @MessageMapping("/heartbeat")
    public void heartbeat(Principal principal){
        //cachservice.setuserstatus(true,principal.getName());
       // cachservice.setuserlastseen(principal.getName());
    }
}
