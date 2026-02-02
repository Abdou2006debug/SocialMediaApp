package com.example.whatsappclone.Messaging.application;

import com.example.whatsappclone.Messaging.api.dto.chatDTO;
import com.example.whatsappclone.Messaging.api.dto.chatMemberDTO;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.domain.ChatMember;
import com.example.whatsappclone.Messaging.persistence.ChatMemberRepo;
import com.example.whatsappclone.Messaging.persistence.ChatRepo;
import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.application.ProfileSummaryBuilder;
import com.example.whatsappclone.Shared.Exceptions.ChatNotFoundException;
import com.example.whatsappclone.Shared.Mappers.Chatmapper;
import com.example.whatsappclone.User.application.AuthenticatedUserService;
import com.example.whatsappclone.User.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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


    public List<chatDTO> getUserChats(int page){
        User currentUser=authenticatedUserService.getcurrentuser();
        Pageable pageable= PageRequest.of(page,10);
        Page<Chat> Page = chatRepo.findByUser(currentUser.getUuid(),pageable);
        List<Chat> chats = Page.getContent();
        List<chatMemberDTO> otherChatMembers=chatMemberRepo.findOtherChatMembers(chats.stream().map(Chat::getUuid).toList(),currentUser.getUuid());
        List<profileSummary> profileSummaries= profileSummaryBuilder.buildProfileSummaries(otherChatMembers.stream().map(chatMemberDTO::getUser_Id).toList());
        Map<String,chatMemberDTO> map=otherChatMembers.stream()
                .collect(Collectors.toMap(chatMemberDTO::getUser_Id, Function.identity()));
        List<chatDTO> chatDTOS= profileSummaries.stream().map(profileSummary -> {
            chatMemberDTO chatMemberDTO=map.get(profileSummary.getUserId());
            chatDTO chatDTO=chatmapper.tochatDTO(profileSummary);
            chatDTO.setChatId(chatMemberDTO.getChat_Id());
            return chatDTO;
        }).toList();
        chatStatusResolver.computeStatus(chats,chatDTOS,currentUser.getUuid());
        return chatDTOS;
    }

    public void getUserChat(String chatId){
        Chat chat= chatRepo.findById(chatId).orElseThrow(()-> new ChatNotFoundException("Chat not found"));
    }






}
