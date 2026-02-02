package com.example.whatsappclone.Messaging.domain;

import com.example.whatsappclone.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(indexes={
        @Index(name="chat_userid",columnList = "user_id")
})
public class ChatMember {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chat_id",updatable = false,insertable = false)
    private String chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public ChatMember(Chat chat,User user){
        this.chat=chat;
        this.user=user;
        }
        private String chatView;
}
