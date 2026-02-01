package com.example.whatsappclone.Messaging.domain;

import jakarta.persistence.Id;
import jakarta.persistence.Index;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.*;
import java.time.Instant;

@Document(collection = "message")
@Data
public class Message {
    @Id
    private String id;
    @Indexed
    private String chatId;
    private String senderId;
    private MessageType messageType=MessageType.TEXT;
    private String content;
    private String sentAt= Instant.now().toString();
    private boolean edited=false;
    private boolean read=false;
}
