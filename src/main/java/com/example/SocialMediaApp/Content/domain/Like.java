package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Like {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private LikeType type;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne
    @JoinColumn(name= "user_id")
    private User user;

    // like record can only belong to either comment or story
    @PrePersist
    public void prePersist(){
        if(comment!=null&&story!=null){
            throw new RuntimeException();
        }
    }

    @PreUpdate
    public void preUpdate(){
        if(type!=null){
            throw new RuntimeException();
        }
    }
}
