package com.example.whatsappclone.Notification.domain;

import com.example.whatsappclone.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Table(indexes = {@Index(name="userNotifications",columnList = "user_id")})
public class NotificationsSettings {
    @Id
    @GeneratedValue
    private UUID id;
   // @Column(name="likes")
    //private Boolean Onlikes;
    //@Column(name = "comments")
    //private Boolean Oncomments;
    //@Column(name="commentreplies")
    //private Boolean Oncommentreplies;

    @Column(name="follow")
    private Boolean Onfollow;

    @Column(name="followingrequests")
    private Boolean onfollowingrequestAccepted;

    @Column(name="followingrequests_rejected")
    private Boolean onfollowingrequestRejected;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}