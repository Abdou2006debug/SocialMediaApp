package com.example.whatsappclone.SocialGraph.domain;

import com.example.whatsappclone.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@Table(indexes = {
        @Index(name="block",columnList = "blocked_id,blocker_id")
})
public class Blocks {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @CreatedDate
    private Instant blockedat;
    @ManyToOne
    @JoinColumn(name = "blocker_id")
    private User blocker;
    @ManyToOne
    @JoinColumn(name="blocked_id")
    private User blocked;
    public Blocks(User blocker, User blocked){
        this.blocked=blocked;
        this.blocker=blocker;
    }
}
