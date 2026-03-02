package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
@Table(indexes ={
        @Index(name ="story_user",columnList = "user_id")
})
public class Story {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @CreatedDate
    private Instant createdAt;

    private Instant publishedAt;

    private Instant expiresAt;

    private Long likeCount=0L;

    private Long viewCount=0L;

    private  StoryStatus storyStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Media media;



    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private StorySettings storySettings;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User user;

    public enum StoryStatus{
        PUBLISHED,DELETED,DRAFT;
    }

    public Story(String id){
        this.id=id;
    }

}
