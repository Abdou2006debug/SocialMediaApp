package com.example.whatsappclone.Messaging.application;

import com.example.whatsappclone.Messaging.api.dto.chatDTO;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.domain.ChatMember;
import com.example.whatsappclone.Messaging.domain.Message;
import com.example.whatsappclone.Messaging.persistence.ChatMemberRepo;
import com.example.whatsappclone.Messaging.persistence.MessageRepo;
import com.example.whatsappclone.User.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatusResolver {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;


    public void computeStatus(List<Chat> chats, List<chatDTO> chatDTOS,String currentId){

        Map<String,chatDTO> dtomap= chatDTOS.stream()
                .collect(Collectors.toMap(chatDTO::getChatId, Function.identity()));

        List<String> lastmessageIds=chats.stream().map(Chat::getLastMessageId).collect(Collectors.toList());

        List<Message> lastmessages= messageRepo.findByIdIn(lastmessageIds);

        List<String> unProcessedChats=new ArrayList<>();

        for(Message message:lastmessages){
            chatDTO chatDTO=dtomap.get(message.getChatId());

            if(chatDTO==null) continue;

            if(message.getSenderId().equals(currentId)){
                if(message.isRead()){
                    chatDTO.setChatView("seen:"+message.getReadAt());
                }else{
                    chatDTO.setChatView("sent:"+message.getSentAt());
                }
            }else if(message.isRead()){
                chatDTO.setChatView(message.getContent()+":"+message.getSentAt());
            }else {
                unProcessedChats.add(message.getChatId());
            }
        }

        if(unProcessedChats.isEmpty()) return;

        List<ChatMember> chatMembers= chatMemberRepo.findByUserAndChatIdIn(new User(currentId),unProcessedChats);

        for(ChatMember chatMember:chatMembers){
            String chatId=chatMember.getChatId();
            chatDTO chatDTO= dtomap.get(chatId);
            if(chatDTO==null) continue;
            chatDTO.setChatView("+"+chatMember.getUnreadCount()+" New Messages");
        }

    }
}

