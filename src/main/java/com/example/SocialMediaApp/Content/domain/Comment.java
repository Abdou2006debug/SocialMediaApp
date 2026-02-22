package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@Table(indexes ={
        @Index(name ="post_comment",columnList = "post_id")
})
public class Comment {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @CreatedDate
    private Instant createdAt;

    @Max(100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Post post;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User user;

    public Comment(String content,String userId,String postId){
        this.content=content;
        this.user=new User(userId);
        this.post=new Post(postId);
    }

}
