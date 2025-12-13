package com.example.whatsappclone.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name="USERS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @CreatedDate
    private Instant createddate;
    @LastModifiedDate
    private Instant lastmodifieddate;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String username;
    @Email
    @Column(unique = true)
    private String email;
    private String keycloakId;
    private LocalDate birthday;
    @ManyToMany
    @JoinTable(
            name = "user_chats",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id")
    )
    private List<Chat> chats=new ArrayList<>();
    public User(String username,String firstname,String lastname,String email,String keycloakid){
        this.firstname=firstname;
        this.lastname=lastname;
        this.email=email;
        this.keycloakId=keycloakid;
        this.username=username;
    }
    public User(String username,String uuid){
        this.username=username;
        this.uuid=uuid;
    }
}
