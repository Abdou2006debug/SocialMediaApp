package com.example.SocialMediaApp.Messaging.api.Controllers;

import com.example.SocialMediaApp.Messaging.api.dto.chatDetails;
import com.example.SocialMediaApp.Messaging.api.dto.chatSummary;
import com.example.SocialMediaApp.Messaging.application.ChatOverviewService;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/chats/overview")
@Validated
public class ChatOverviewController {

    private final ChatOverviewService chatOverviewService;

    @GetMapping
    public List<chatSummary> getChats(@RequestParam(defaultValue = "0") @PositiveOrZero int page){
        return chatOverviewService.getUserChats(page);
    }

    @GetMapping("/{chatId}")
    public chatDetails getChat(@PathVariable String chatId,
                                @RequestParam(defaultValue = "0") @PositiveOrZero  int page){

        return chatOverviewService.getUserChat(chatId,page);
    }


}
