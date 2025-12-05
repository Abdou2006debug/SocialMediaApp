package com.example.whatsappclone.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String bio;
    private String username;
    private String privateavatarurl;
    private String publicavatarurl;
    private boolean showifonline=false;
    private boolean isprivate=false;
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
public Profile(String bio ,String username){
    this.bio=bio;
    this.username=username;
}
}
