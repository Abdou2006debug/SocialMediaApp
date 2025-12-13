package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.Services.CachService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class realtimecontroller {
    private final CachService cachservice;
    @MessageMapping("/heartbeat")
    public void heartbeat(Principal principal){
        cachservice.setuserstatus(true,principal.getName());
        cachservice.setuserlastseen(principal.getName());
    }
}
