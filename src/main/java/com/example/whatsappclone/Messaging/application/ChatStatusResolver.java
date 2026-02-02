package com.example.whatsappclone.Messaging.application;

import com.example.whatsappclone.Messaging.api.dto.chatDTO;
import com.example.whatsappclone.Messaging.api.dto.chatMemberDTO;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.persistence.MessageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatusResolver {

    private final MessageRepo messageRepo;

    public void computeStatus(List<Chat> chats, List<chatDTO> chatDTOS,String senderId){
        Map<String,chatDTO> map= chatDTOS.stream()
                .collect(Collectors.toMap(chatDTO::getChatId, Function.identity()));
        // this gets the last message sent in each of the chats passed
        List<String> lastmessageIds=chats.stream().map(Chat::getLastMessageId).toList();
        messageRepo.findByIdInAndSenderId(lastmessageIds,senderId).forEach(message -> {
            chatDTO chatDTO=map.get(message.getChatId());
            if(message.isRead()){
               chatDTO.setChatView("seen");
            }
        });

    }
}
