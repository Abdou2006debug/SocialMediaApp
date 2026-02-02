package com.example.whatsappclone.Messaging.persistence;

import com.example.whatsappclone.Messaging.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepo extends MongoRepository<Message,String> {
    List<Message> findByIdIn(List<String> ids);

    Page<Message> findByChatId(String chatId, Pageable pageable);
}
