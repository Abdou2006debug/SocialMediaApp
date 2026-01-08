package com.example.whatsappclone.Notification.domain;

import com.example.whatsappclone.Identity.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Table(indexes = {@Index(name="userNotifications",columnList = "user_id")})
public class NotificationsSettings {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
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