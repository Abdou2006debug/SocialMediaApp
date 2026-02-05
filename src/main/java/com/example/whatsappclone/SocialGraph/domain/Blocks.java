package com.example.whatsappclone.SocialGraph.domain;

import com.example.whatsappclone.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Data
@Table(indexes = {
        @Index(name="block",columnList = "blocked_id,blocker_id")
})
public class Blocks {

    @Id
    @GeneratedValue
    private UUID id;

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
