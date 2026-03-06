package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@Table(indexes ={
        @Index(name = "comment_reply",columnList = "comment_id")
})
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Size(max = 100)
    private String content;

    private Long likeCount=0L;

    @ManyToOne
    @JoinColumn(name="comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name ="user_id")
    private User user;

    public Reply(String userId,String commentId,String content){
        this.user=new User(userId);
        this.comment=new Comment(commentId);
        this.content=content;
    }

}
