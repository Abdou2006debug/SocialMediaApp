package com.example.whatsappclone.Messaging.persistence;

import com.example.whatsappclone.Messaging.api.dto.chatMemberDTO;
import com.example.whatsappclone.Messaging.domain.Chat;
import com.example.whatsappclone.Messaging.domain.ChatMember;
import com.example.whatsappclone.User.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMemberRepo extends JpaRepository<ChatMember,UUID> {
    @Query(value = "SELECT user_id, chat_id FROM chatMember WHERE chat_id IN (:chatIds) AND user_id != :userId", nativeQuery = true)
    List<chatMemberDTO> findOtherChatMembers(@Param("chatIds") List<String> chatIds, @Param("userId") String userId);
}
