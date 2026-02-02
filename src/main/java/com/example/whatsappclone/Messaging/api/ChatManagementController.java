package com.example.whatsappclone.Messaging.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/chats")
public class ChatManagementController {

    private final ChatManagementController chatManagementController;

}
