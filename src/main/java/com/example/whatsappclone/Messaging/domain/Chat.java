package com.example.whatsappclone.Messaging.domain;


import com.example.whatsappclone.Identity.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @CreatedDate
    private Instant createddate;
    @ManyToOne()
    private User creator;
    @ManyToMany(mappedBy = "chats")
    private Set<User> users=new HashSet<>();
    private boolean isgroup;
    public Chat(User creator, boolean isgroup){
        this.isgroup=isgroup;
        this.creator=creator;
    }
}
