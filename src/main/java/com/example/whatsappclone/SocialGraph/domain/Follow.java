package com.example.whatsappclone.SocialGraph.domain;

import com.example.whatsappclone.Identity.domain.User;
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
@Table(indexes ={
        @Index(name ="follower_following",columnList = "follower_id,following_id,status",unique = true),
        @Index(name="follower",columnList = "follower_id,status"),
        @Index(name="following",columnList = "following_id,status"),
} )
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

    @Column(name = "follower_id",insertable = false,updatable = false)
    private String follower_id;

    @Column(name = "follower_id",insertable = false,updatable = false)
    private String following_id;

    public Follow(User follower, User following){
       this.follower=follower;
       this.following=following;
    }
    public Follow(User follower, User following, Status status){
        this(follower,following);
        this.status=status;
    }
    public enum Status{PENDING,ACCEPTED}
}
