package com.example.SocialMediaApp.Messaging.api;

import com.example.SocialMediaApp.Messaging.api.dto.chatHearbeatDTO;
import com.example.SocialMediaApp.Messaging.api.dto.sendMessageToChatDTO;
import com.example.SocialMediaApp.Messaging.application.ChatActivityTracker;
import com.example.SocialMediaApp.Messaging.application.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealTimeChatController {

    private final ChatActivityTracker chatActivityTracker;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.join")
    public void join(Principal principal, chatHearbeatDTO chatHearbeatDTO){
        chatActivityTracker.setChat_UserStatus(principal.getName(),chatHearbeatDTO.getChatId());
    }

    @MessageMapping("chat.send")
    public void sendMessage(Principal principal, sendMessageToChatDTO sendMessageToChatDTO){
        chatMessageService.sendMessageToChat(principal,sendMessageToChatDTO);
    }

}
