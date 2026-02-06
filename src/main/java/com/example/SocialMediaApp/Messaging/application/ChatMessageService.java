package com.example.SocialMediaApp.Messaging.application;

import com.example.SocialMediaApp.Messaging.api.dto.sendMessageToChatDTO;
import com.example.SocialMediaApp.Messaging.api.dto.sendMessageToUserDTO;
import com.example.SocialMediaApp.Messaging.domain.Chat;
import com.example.SocialMediaApp.Messaging.domain.ChatMember;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Messaging.persistence.ChatMemberRepo;
import com.example.SocialMediaApp.Messaging.persistence.ChatRepo;
import com.example.SocialMediaApp.Messaging.persistence.MessageRepo;
import com.example.SocialMediaApp.Profile.application.cache.ProfileCacheManager;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Exceptions.ChatMessagingException;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final BlocksRepo blocksRepo;
    private final ProfileCacheManager profileCacheManager;
    private final ChatRepo chatRepo;

    // this is meant for first time chatting it first check whether a chat exists between users or not
    @CheckUserExistence
    public void sendMessageToUser(sendMessageToUserDTO sendMessageToUserDTO){
        User currentUser=authenticatedUserService.getcurrentuser();
        User targetUser=new User(sendMessageToUserDTO.getUserId());
        if(blocksRepo.existsByBlockerAndBlocked(currentUser,targetUser)||blocksRepo.existsByBlockerAndBlocked(targetUser,currentUser)){
            throw new ChatMessagingException("cannot send message to user");
        }



        // checking if there is a chat between them


        try{
        Chat chat=chatRepo.save(new Chat());
        ChatMember chatMember1=new ChatMember(chat,currentUser);
        ChatMember chatMember2=new ChatMember(chat,targetUser);
        chatMember2.incrementUnreadCount();
        chatMemberRepo.saveAll(List.of(chatMember1,chatMember2));
        Message message=messageRepo.save(new Message(chat.getId(),currentUser.getId(), sendMessageToUserDTO.getContent()));
        chat.setLastMessageId(message.getId());
        chat.setLastMessageAt(message.getSentAt());
        chatRepo.save(chat);
        }catch (Exception e){
            log.error("sending message to user failed "+e.getMessage());
            throw new ChatMessagingException("could not send message to user");
        }
    }


    @CheckUserExistence
    public void sendMessageToChat(sendMessageToChatDTO sendMessageToChatDTO){

    }

}
