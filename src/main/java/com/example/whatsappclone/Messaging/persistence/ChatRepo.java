package com.example.whatsappclone.Messaging.persistence;

import com.example.whatsappclone.Messaging.domain.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepo extends JpaRepository<Chat,String> {
    @Query(""" 
    SELECT c
    FROM Chat c
    JOIN c.members cm
    WHERE cm.user.uuid = :userId
    ORDER BY c.lastMessageAt DESC
""")
    Page<Chat> findByUser(String userId, Pageable pageable);
}
