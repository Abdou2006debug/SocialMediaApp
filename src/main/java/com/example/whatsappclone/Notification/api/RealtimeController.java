package com.example.whatsappclone.Notification.api;

import com.example.whatsappclone.User.application.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealtimeController {
    private final UserActivityService userActivityService;

    @MessageMapping("/heartbeat")
    public void heartbeat(Principal principal){
        userActivityService.setuserstatus(true,principal.getName());
        userActivityService.setUserLastSeen(principal.getName());
    }

}
