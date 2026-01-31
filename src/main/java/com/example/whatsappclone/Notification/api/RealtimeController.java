package com.example.whatsappclone.Notification.api;

import com.example.whatsappclone.User.application.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeController {
    private final UserActivityService userActivityService;

    @MessageMapping("/heartbeat")
    public void heartbeat(Principal principal){
        log.info("heartbeat "+principal.getName());
        userActivityService.setuserstatus(true,principal.getName());
        userActivityService.setUserLastSeen(principal.getName());
    }

}
