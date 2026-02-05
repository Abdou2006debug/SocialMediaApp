package com.example.whatsappclone.Messaging.application;

import com.example.whatsappclone.Messaging.api.dto.*;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.domain.ChatMember;
import com.example.whatsappclone.Messaging.domain.Message;
import com.example.whatsappclone.Messaging.persistence.ChatMemberRepo;
import com.example.whatsappclone.Messaging.persistence.ChatRepo;
import com.example.whatsappclone.Messaging.persistence.MessageRepo;
import com.example.whatsappclone.Notification.persistence.NotificationSettingsRepo;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.application.ProfileSummaryBuilder;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import com.example.whatsappclone.Shared.Exceptions.ChatNotFoundException;
import com.example.whatsappclone.Shared.Mappers.Chatmapper;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.application.UserActivityService;
import com.example.whatsappclone.User.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatManagmentService {

    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileSummaryBuilder profileSummaryBuilder;
    private final ChatRepo chatRepo;
    private final Chatmapper chatmapper;
    private final ChatStatusResolver chatStatusResolver;
    private final MessageRepo messageRepo;
    private final UserActivityService userActivityService;
    private final ProfileCacheManager profileCacheManager;

    public List<chatSummary> getUserChats(int page){
        User currentUser=authenticatedUserService.getcurrentuser();
        Pageable pageable= PageRequest.of(page,10);
        Page<Chat> Page = chatRepo.findByUser(currentUser.getId(),pageable);
        List<Chat> chats = Page.getContent();
        List<chatMemberDTO> otherChatMembers=chatMemberRepo.findOtherChatMembers(chats.stream().map(Chat::getId).toList(),currentUser.getId());
        List<profileSummary> profileSummaries= profileSummaryBuilder.buildProfileSummaries(otherChatMembers.stream().map(chatMemberDTO::getUser_Id).toList());
        Map<String,chatMemberDTO> map=otherChatMembers.stream()
                .collect(Collectors.toMap(chatMemberDTO::getUser_Id, Function.identity()));
        List<chatSummary> chatSummaries= profileSummaries.stream().map(profileSummary -> {
            chatMemberDTO chatMemberDTO=map.get(profileSummary.getUserId());
            chatSummary chatDTO=chatmapper.tochatDTO(profileSummary);
            chatDTO.setChatId(chatMemberDTO.getChat_Id());
            return chatDTO;
        }).toList();
        chatStatusResolver.computeStatus(chats,chatSummaries,currentUser.getId());
        return chatSummaries;
    }

    public chatDetails getUserChat(String chatId, int page){
        User currentUser=authenticatedUserService.getcurrentuser();

        if(!chatRepo.existsById(chatId)||!chatMemberRepo.existsByUserAndChatId(currentUser,chatId)){
            throw new ChatNotFoundException("Chat not found");
        }

        ChatMember otherchatMember=chatMemberRepo.findByChatIdAndUserIdNot(chatId,currentUser.getId()).orElseThrow();
        chatUser chatUser=buildChatUser(otherchatMember);
        Pageable pageable=PageRequest.of(page,10, Sort.by(Sort.Direction.DESC,"lastMessageAt"));
        Page<Message> Page= messageRepo.findByChatId(chatId,pageable);
          List<Message> messages= Page.getContent();
          List<messageDTO> messageDTOS=new ArrayList<>();

          for (Message message:messages){
              messageDTO messageDTO=chatmapper.tomessageDTO(message);

              messageDTO.setSentByme(message.getSenderId().equals(currentUser.getId()));

              if(message.getId().equals(otherchatMember.getLastreadMessageId())){
                  messageDTO.setLastView(true);
              }

              messageDTOS.add(messageDTO);
          }

          if(page==0&&!messages.isEmpty()){
             chatMemberRepo.updateUserChat(messages.get(0).getId(),currentUser.getId(),chatId);
          }

          return new chatDetails(chatId,chatUser,messageDTOS);
    }

    private chatUser buildChatUser(ChatMember chatMember){
        String otherchatMemberId=chatMember.getUserId();
        Profile profile=profileCacheManager.getProfile(otherchatMemberId).get();
        Boolean online=null;
        String lastActivity=null;
        if(profile.isShowifonline()){
            if(userActivityService.getUserStatus(otherchatMemberId)){
                online=true;
            }else{
                lastActivity=userActivityService.getUserLastSeen(otherchatMemberId);
            }
        }
        chatUser chatUser=chatmapper.tochatUser(profile);
        chatUser.setOnline(online);
        chatUser.setLastActivity(lastActivity);
        return chatUser;
    }
}
