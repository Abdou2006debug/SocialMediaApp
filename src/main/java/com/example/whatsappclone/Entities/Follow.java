package com.example.whatsappclone.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
@NoArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @CreatedDate
    private Instant createddate;
    private Instant accepteddate;
    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="follower_id")
    private User follower;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="following_id")
    private User following;
    @Enumerated(EnumType.STRING)
    private Status status;
    public Follow( User follower, User following){
       this.follower=follower;
       this.following=following;
    }
    public Follow(User follower,User following,Status status){
        this(follower,following);
        this.status=status;
    }
    public enum Status{PENDING,ACCEPTED}
}
