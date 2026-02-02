package com.example.whatsappclone.Messaging.application;

import com.example.whatsappclone.Messaging.api.dto.chatDTO;
import com.example.whatsappclone.Messaging.api.dto.chatMemberDTO;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.domain.Message;
import com.example.whatsappclone.Messaging.domain.MessageType;
import com.example.whatsappclone.Messaging.persistence.MessageRepo;
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

    public void computeStatus(List<Chat> chats, List<chatDTO> chatDTOS,String senderId){
        Map<String,chatDTO> map= chatDTOS.stream()
                .collect(Collectors.toMap(chatDTO::getChatId, Function.identity()));
        // this gets the last message sent in each of the chats passed
        List<String> lastmessageIds=chats.stream().map(Chat::getLastMessageId).toList();

       List<Message> lastmessages= messageRepo.findByIdIn(lastmessageIds);

       List<Message> messagesNotByUser=new ArrayList<>();

       // handling the case where the last message is sent by the user so it can be seen or not (sent)
       lastmessages.forEach(message -> {
           if(message.getSenderId().equals(senderId)){
               chatDTO chatDTO=map.get(message.getChatId());
               if(message.isRead()){
                   chatDTO.setChatView("seen:"+message.getReadAt());
               }else{
                   chatDTO.setChatView("sent:"+message.getSentAt());
               }
               map.remove(message.getChatId());
           }else{
               // if the message is not sent by the same user add it to this list to handle it
               messagesNotByUser.add(message);
           }
                });
              //  compute(messagesNotByUser,);

    }

        public void compute(List<Message> messages,Map<String,chatDTO> chatDTOS){
        List<Message> unreadMessages=new ArrayList<>();
        messages.forEach(message -> {
            if(message.isRead()){
                chatDTOS.get(message.getChatId()).setChatView(message.getContent()+" "+message.getSentAt());
            }else{
                unreadMessages.add(message);
            }
        });

     //   messageRepo.findByChatId(unreadMessages.stream().map(Message::getChatId).toList());
        boolean end=false;
        int counter=4;
        while(!end&&counter>0){
           // messageRepo.

            //counter--;
        }
    }

}
